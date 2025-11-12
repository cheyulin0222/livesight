package com.arplanets.jwt.service;

import com.arplanets.jwt.db.arplanetSys.entity.ServiceInteractionModuleBind;
import com.arplanets.jwt.db.arplanetSys.entity.ServiceInteractionTrigger;
import com.arplanets.jwt.db.arplanetSys.entity.ServiceInteractionModule;
import com.arplanets.jwt.db.arplanetSys.repository.ServiceInteractionModuleBindRepository;
import com.arplanets.jwt.db.arplanetSys.repository.ServiceInteractionTriggerRepository;
import com.arplanets.jwt.db.arplanetSys.repository.ServiceModuleRepository;
import com.arplanets.jwt.db.arplanetSysLog.entity.ServiceUserTicket;
import com.arplanets.jwt.db.arplanetSysLog.repository.ServiceUserRepository;
import com.arplanets.jwt.db.arplanetSysLog.repository.ServiceUserTicketRepository;
import com.arplanets.jwt.dto.ModuleContentUrlDto;
import com.arplanets.jwt.dto.PayloadDto;
import com.arplanets.jwt.dto.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);

    // --- (DB 1: sys_log) 的 Repositories ---
    private final ServiceUserRepository serviceUserRepository;
    private final ServiceUserTicketRepository ticketRepository;

    // --- (DB 2: arplanet_sys) 的 Repositories ---
    private final ServiceInteractionTriggerRepository triggerRepository;
    private final ServiceInteractionModuleBindRepository bindRepository;
    private final ServiceModuleRepository moduleRepository;
    private final RestTemplate restTemplate;

    // --- 其他工具 ---
    private final ObjectMapper objectMapper;

    public Optional<ValidationResult> validateAndProcess(PayloadDto payload) {

        // --- 邏輯 A: 驗證 User 和 Ticket (在 DB 1) ---
        // (此邏輯保持您提供的版本不變)
        Optional<ServiceUserTicket> validTicketOpt = validateUserAndTicket(
                payload.getAuthTypeId(),
                payload.getTicketObjid()
        );

        System.out.println(validTicketOpt);
        if (validTicketOpt.isEmpty()) {
            logger.warn("User/Ticket 驗證失敗 (User 或 Ticket 不存在)。 AuthID: {}, TicketID: {}",
                    payload.getAuthTypeId(), payload.getTicketObjid());
            return Optional.empty();
        }

        // --- 邏輯 B: 查找 Final URL (在 DB 2) ---
        ServiceUserTicket validTicket = validTicketOpt.get();

        Optional<String> finalUrlOpt = findFinalUrl(validTicket.getTicketId());

        if (finalUrlOpt.isEmpty()) {
            // (*** 這裡已修改 ***) 更新錯誤日誌
            logger.error("嚴重錯誤：配置找不到！ Ticket ID (來自 DB 1 的 ticket_id 欄位): {} 的 URL 查找失敗。", validTicket.getTicketId());
            // (*** 這裡已修改 ***) 更新錯誤訊息
            throw new IllegalStateException("Module configuration not found for ticket_id: " + validTicket.getTicketId());
        }

        // --- 成功 ---
        return Optional.of(new ValidationResult(
                finalUrlOpt.get(),
                payload.getAuthTypeId()
        ));
    }

    // 您的 "只檢查存在" 邏輯
    @Transactional(transactionManager = "sysLogTransactionManager", readOnly = true)
    public Optional<ServiceUserTicket> validateUserAndTicket(String authTypeId, long ticketObjid) {

        // 步驟 1: 檢查 User 是否存在 (DB 1)
        if (!serviceUserRepository.existsByAuthTypeId(authTypeId)) {
            logger.warn("驗證失敗：ServiceUser (auth_type_id = {}) 不存在。", authTypeId);
            return Optional.empty(); // User 不存在
        }

        // 步驟 2: 檢查 Ticket (DB 1)
        Optional<ServiceUserTicket> ticketOpt = ticketRepository.findByTicketObjid(ticketObjid);
        if (ticketOpt.isEmpty()) {
            logger.warn("驗證失敗：ServiceUserTicket (ticket_objid = {}) 不存在。", ticketObjid);
            return Optional.empty(); // Ticket 不存在
        }

        logger.info("Ticket (ticket_objid = {}) 驗證通過 (存在)。", ticketObjid);

        return ticketOpt;
    }

    @Transactional(transactionManager = "arplanetSysTransactionManager", readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Optional<String> findFinalUrl(long triggerId) {
        try {

            String triggerIdString = String.valueOf(triggerId);
            ServiceInteractionTrigger trigger = triggerRepository
                    .findByTriggerTypeAndTriggerTypeId("ticket", triggerIdString)
                    .orElseThrow(() -> new RuntimeException("在 DB 2 找不到 Trigger (type=ticket, id=" + triggerIdString + ")"));

            long interactionId = trigger.getInteractionId();
            ServiceInteractionModuleBind bind = bindRepository
                    .findFirstByInteractionId(interactionId)
                    .orElseThrow(() -> new RuntimeException("在 DB 2 找不到 Bind (interaction_id=" + interactionId + ")"));

            long moduleId = bind.getModuleId();
            ServiceInteractionModule module = moduleRepository
                    .findByModuleId(moduleId)
                    .orElseThrow(() -> new RuntimeException("在 DB 2 找不到 Module (module_id=" + moduleId + ")"));

            String s3Url = module.getModuleContentUrl();

            if (s3Url == null || s3Url.isBlank()) {
                throw new RuntimeException("Module (id=" + moduleId + ") 的 modu_content_url 欄位是 NULL 或空白的。");
            }

            logger.info("從 DB 2 取得 S3 URL: {}", s3Url);

            // --- 步驟 2: (新) 從 S3 下載 JSON 內容 ---

            // 使用 RestTemplate (HTTP 客戶端) 向 S3 發起 GET 請求
            // (注意：S3 檔案必須是 "公開可讀"，否則這裡會失敗)
            String s3FileContent = restTemplate.getForObject(s3Url, String.class);

            if (s3FileContent == null || s3FileContent.isBlank()) {
                throw new RuntimeException("S3 檔案 (" + s3Url + ") 的內容是空的。");
            }

            logger.info("從 S3 下載的 JSON 內容: {}", s3FileContent);

            ModuleContentUrlDto contentDto = objectMapper.readValue(s3FileContent, ModuleContentUrlDto.class);

            if (contentDto.getAttr() == null ||
                    contentDto.getAttr().getHyperlink() == null ||
                    contentDto.getAttr().getHyperlink().isBlank()) {
                throw new RuntimeException("S3 檔案 JSON 中缺少 'attr.hyperlink' 欄位，或該欄位為空。");
            }

            // 3. 取得巢狀 URL
            String finalUrl = contentDto.getAttr().getHyperlink();

            // 4. 返回 "hyperlink" 的值
            return Optional.of(finalUrl);
        } catch (Exception e) {
            // (這會捕捉到 DB 錯誤、S3 下載錯誤、JSON 解析錯誤)
            logger.error("查找 Final URL 時發生嚴重錯誤 (triggerId/ticket_id = {}): {}", triggerId, e.getMessage());
            return Optional.empty();
        }
    }
}