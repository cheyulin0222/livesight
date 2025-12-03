package com.arplanets.corexrapi.livesight.service.impl;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.enums.OrderErrorCode;
import com.arplanets.corexrapi.livesight.log.ErrorContext;
import com.arplanets.corexrapi.livesight.log.Logger;
import com.arplanets.corexrapi.livesight.log.LoggingService;
import com.arplanets.corexrapi.livesight.model.dto.ClientInfo;
import com.arplanets.corexrapi.livesight.mapper.OrderMapper;
import com.arplanets.corexrapi.livesight.model.dto.ResponseContext;
import com.arplanets.corexrapi.livesight.model.bo.OrderIotPayload;
import com.arplanets.corexrapi.livesight.model.dto.LiveSightDto;
import com.arplanets.corexrapi.livesight.model.dto.OrderDto;
import com.arplanets.corexrapi.livesight.model.dto.req.OrderFilterRequest;
import com.arplanets.corexrapi.livesight.model.dto.req.PageRequest;
import com.arplanets.corexrapi.livesight.model.dto.res.PageResult;
import com.arplanets.corexrapi.livesight.model.eunms.OrderStatus;
import com.arplanets.corexrapi.livesight.model.po.OrderPo;
import com.arplanets.corexrapi.livesight.repository.OrderRepository;
import com.arplanets.corexrapi.livesight.security.jwt.OrderJwtManager;
import com.arplanets.corexrapi.livesight.service.IotService;
import com.arplanets.corexrapi.livesight.service.LiveSightService;
import com.arplanets.corexrapi.livesight.service.OrderService;
import com.arplanets.commons.utils.ClientInfoUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.SdkBytes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbOrderServiceImpl implements OrderService {

    @Value("${order.create.expiration-minutes}")
    private long expirationMinutes;
    @Value("${order.access-token.expiration-minutes}")
    private long jwtExpirationMinutes;
    @Value("${order.ttl.minutes}")
    private long ttlMinutes;
    @Value("${order.redeem-code.expiration-minutes}")
    private long redeemCodeExpirationMinutes;

    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Taipei");
    public static final String LIVE_SIGHT_NAME = "livesight";
    public static final String ORDER_PREFIX = "order";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final LiveSightService liveSightService;
    private final OrderJwtManager orderJwtManager;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;
    private final IotService iotService;
    private final LoggingService loggingService;

    @Override
    public OrderDto createOrder(HttpServletRequest request, String productId, String namespace, String authType, String authTypeId, String salt) {
        // 產生 Order ID
        String orderId = ORDER_PREFIX + "_" + UUID.randomUUID();

        // 以 Order ID 、 Salt 產生 Verification Code
        String verificationCode = hashWithSHA256(orderId, salt);

        // 取得 Client 資訊
        ClientInfo clientInfo = ClientInfoUtil.getClientInfo(request);

        // 新增訂單資料
        OrderPo result = orderRepository.create(buildCreatedOrder(orderId, namespace, productId, authType, authTypeId, clientInfo, verificationCode));

        // 將訂單資料暫存以做 Audit Log
        setResponseContext(request, result);

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);
    }

    @Override
    public OrderDto getOrderStatus(String productId, String orderId, String salt) {
        // 取得訂單資訊，若無拋出錯誤
        OrderPo result = findOrThrowByOrderId(orderId);

        // 驗證 salt
        validateSalt(orderId, salt, result.getVerificationCode());

        // 驗證 Product ID
        validateProductId(productId, result.getProductId());

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);
    }

    @Override
    public OrderDto getOrder(String productId, String orgId, String namespace, String orderId) {
        // 取得訂單資訊，若無拋出錯誤
        OrderPo result = findOrThrowByOrderId(orderId);

        // 驗證訂單是否為該 Live Sight
        validateOrderInLiveSight(namespace, result);

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);
    }



    @Override
    public OrderDto activateOrder(HttpServletRequest request, String productId, String orgId, String namespace, String orderId, List<String> tags, String staffId) {
        // 產生 Redeem Code
        String redeemCode = genRedeemCode();

        // 修改訂單資料
        OrderPo result = orderRepository.update(buildActivatedOrder(orderId, namespace, productId, staffId, redeemCode, tags));

        // 非同步將訂單資訊傳到 Iot
        iotService.sendIotRequest(buildTopicPath(result), buildPayload(result), loggingService.initApiMessage(result.getOrderId()));

        // 將訂單資料暫存以做 Audit Log
        setResponseContext(request, result);

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);

    }


    @Override
    public OrderDto redeemOrder(HttpServletRequest request, String productId, String orderId, String redeemCode) {
        // 產生當下時間
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);

        // 產生過期時間
        LocalDate tomorrow = now.toLocalDate().plusDays(1);
        ZonedDateTime expireTime = ZonedDateTime.of(tomorrow, LocalTime.MIDNIGHT, ZONE_ID);

        // 為了取得 tags
        OrderPo order = findOrThrowByOrderId(orderId);

        // 產生 Access Token
        String accessToken = orderJwtManager.genAccessToken(order, now, expireTime);

        // 修改訂單資料
        OrderPo result = orderRepository.update(buildRedeemedOrder(orderId, productId, redeemCode, accessToken, now, expireTime));

        // 將訂單資料暫存以做 Audit Log
        setResponseContext(request, result);

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);

    }

    public OrderDto voidOrder(HttpServletRequest request, String productId, String orgId, String namespace, String orderId, String staffId) {
        // 修改訂單資料
        OrderPo result = orderRepository.update(buildVoidedOrder(orderId, namespace, productId, staffId));

        // 將訂單資訊傳到 Iot
        iotService.sendIotRequest(buildTopicPath(result), buildPayload(result), loggingService.initApiMessage(result.getOrderId()));

        // 將訂單資料暫存以做 Audit Log
        setResponseContext(request, result);

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);
    }

    @Override
    public PageResult<OrderDto> listOrder(String productId, String orgId, String namespace, ZonedDateTime startDate, ZonedDateTime endDate, PageRequest page) {
        // 取得 Live Sight ID
        String liveSightId = extractUuid(namespace);

        // 取得每頁資料數
        Integer pageSize = Optional.ofNullable(page)
                .map(PageRequest::getPageSize)
                .orElse(DEFAULT_PAGE_SIZE);

        // 取得上一筆資料資訊
        String lastEvaluatedKey = Optional.ofNullable(page)
                .map(PageRequest::getLastEvaluatedKey)
                .orElse(null);

        // 取得訂單資料
        PageResult<OrderPo> orderPoPageResult = orderRepository.pageByServiceTypeId(liveSightId, startDate, endDate, pageSize, lastEvaluatedKey);

        // 回傳訂單資訊
        return orderPoPageResult.mapItems(orderMapper::orderPoToOrderDto);
    }

    @Override
    public List<OrderDto> listOrder(String productId, String orgId, String namespace, OrderFilterRequest filters) {
        // 取得 Live Sight ID
        String liveSightId = extractUuid(namespace);

        // 驗證 Org ID
        validateOrg(orgId, liveSightId);

        List<OrderPo> result = orderRepository.listByServiceTypeId(liveSightId, filters);

        return result.stream().map(orderMapper::orderPoToOrderDto).toList();
    }

    @Override
    public OrderDto returnOrder(HttpServletRequest request, String productId, String orgId, String namespace, String orderId, String staffId) {
        // 修改訂單資料
        OrderPo result = orderRepository.update(buildReturnedOrder(orderId, namespace, productId, staffId));

        // 將訂單資訊傳到 Iot
        iotService.sendIotRequest(buildTopicPath(result), buildPayload(result), loggingService.initApiMessage(result.getOrderId()));

        // 將訂單資料暫存以做 Audit Log
        setResponseContext(request, result);

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);
    }

    @Override
    public void verifyToken(String accessToken) {
        // 驗證 Token
        DecodedJWT jwt = orderJwtManager.verify(accessToken);

        String orderId = jwt.getSubject();
        Optional<OrderPo> option = orderRepository.findById(orderId);

        if (option.isEmpty()) {
            throw new OrderApiException(OrderErrorCode._004);
        }

        OrderPo order = option.get();

        OrderStatus orderStatus = order.getOrderStatus();

        if (orderStatus != OrderStatus.REDEEMED) {
            throw new OrderApiException(OrderErrorCode._015);
        }

    }

    private String hashWithSHA256(String orderId, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String combinedString = orderId + salt;
            byte[] hashBytes = digest.digest(combinedString.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    private String extractUuid(String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            return null;
        }

        String[] parts = namespace.split("\\.");

        int livesightIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if (LIVE_SIGHT_NAME.equals(parts[i])) {
                livesightIndex = i;
                break;
            }
        }

        if (livesightIndex != -1 && livesightIndex + 1 < parts.length) {
            return parts[livesightIndex + 1];
        }

        return null;
    }

    private void validateOrg(String orgId, String liveSightId) {
        if (!StringUtils.hasText(liveSightId)) {
            throw new OrderApiException(OrderErrorCode._003);
        }


        LiveSightDto liveSight = liveSightService.getLiveSight(liveSightId);

        if (!orgId.equals(liveSight.getOrgId())) {
            throw new OrderApiException(OrderErrorCode._003);
        }
    }

    private void setResponseContext(HttpServletRequest request, OrderPo order) {
        ResponseContext responseContext = ResponseContext.builder()
                .order(orderMapper.orderPoToOrderContext(order))
                .errorContext(new ErrorContext())
                .build();

        request.setAttribute("responseContext", responseContext);
    }

    private OrderPo findOrThrowByOrderId(String orderId) {
        Optional<OrderPo> option = orderRepository.findById(orderId);
        if (option.isEmpty()) {
            throw new OrderApiException(OrderErrorCode._004);
        }

        return option.get();
    }

    private void validateProductId(String inputProductId, String storedProductId) {
        if (!inputProductId.equals(storedProductId)) {
            throw new OrderApiException(OrderErrorCode._006);
        }
    }

    private String genRedeemCode() {
        // 產生 32 位元組的隨機資料
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        // 使用 URL 安全的 Base64 編碼
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private OrderPo buildCreatedOrder(
            String orderId,
            String namespace,
            String productId,
            String authType,
            String authTypeId,
            ClientInfo clientInfo,
            String verificationCode
    ) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);

        // 產生過期時間
        LocalDate tomorrow = now.toLocalDate().plusDays(1);
        ZonedDateTime expiredAt = ZonedDateTime.of(tomorrow, LocalTime.MIDNIGHT, ZONE_ID);

        return OrderPo.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PENDING)
                .namespace(namespace)
                .productId(productId)
                .serviceType(LIVE_SIGHT_NAME)
                .serviceTypeId(extractUuid(namespace))
                .authType(authType)
                .authTypeId(authTypeId)
                .userBrowser(clientInfo.getBrowserName())
                .userOs(clientInfo.getOsName())
                .userDeviceType(clientInfo.getDeviceType())
                .createdAt(now)
                .verificationCode(verificationCode)
                .expiredAt(expiredAt)
                .updatedAt(now)
                .ttl(now.plusMinutes(ttlMinutes))
                .build();
    }


    private OrderPo buildActivatedOrder(String orderId, String namespace, String productId, String staffId, String redeemCode, List<String> tags) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        ZonedDateTime expiredAt = now.plusMinutes(redeemCodeExpirationMinutes);

        return OrderPo.builder()
                .orderId(orderId)
                .namespace(namespace)
                .productId(productId)
                .orderStatus(OrderStatus.ACTIVATED)
                .activatedAt(now)
                .activatedBy(staffId)
                .redeemCode(redeemCode)
                .tags(tags)
                .expiredAt(expiredAt)
                .updatedAt(now)
                .build();
    }

    private OrderPo buildRedeemedOrder(String orderId, String productId, String redeemCode, String accessToken, ZonedDateTime now, ZonedDateTime expire) {
        return OrderPo.builder()
                .orderId(orderId)
                .productId(productId)
                .orderStatus(OrderStatus.REDEEMED)
                .redeemCode(redeemCode)
                .redeemedAt(now)
                .accessToken(accessToken)
                .expiredAt(expire)
                .updatedAt(now)
                .build();
    }

    private OrderPo buildVoidedOrder(String orderId, String namespace, String productId, String staffId) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);

        return OrderPo.builder()
                .orderId(orderId)
                .namespace(namespace)
                .productId(productId)
                .orderStatus(OrderStatus.VOIDED)
                .voidedAt(now)
                .voidedBy(staffId)
                .updatedAt(now)
                .build();
    }

    private OrderPo buildReturnedOrder(String orderId, String namespace, String productId, String staffId) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        return OrderPo.builder()
                .orderId(orderId)
                .namespace(namespace)
                .productId(productId)
                .orderStatus(OrderStatus.COMPLETED)
                .returnedAt(now)
                .returnedBy(staffId)
                .updatedAt(now)
                .build();
    }

    private void validateSalt(String orderId, String salt, String storedVerificationCode) {
        String currentVerificationCode = hashWithSHA256(orderId, salt);
        if (!currentVerificationCode.equals(storedVerificationCode)) {
            throw new OrderApiException(OrderErrorCode._005);
        }
    }

    private String buildTopicPath(OrderPo orderPo) {
        return getTopicAction(orderPo) + orderPo.getOrderId();
    }

    private String getTopicAction(OrderPo orderPo) {
        OrderStatus orderStatus = orderPo.getOrderStatus();
        return orderStatus == OrderStatus.ACTIVATED ? "active/" : "revoke/";
     }


    private SdkBytes buildPayload(OrderPo orderPo) {
        OrderIotPayload orderPayload = orderMapper.orderPoToOrderIotPayload(orderPo);

        SdkBytes payload = null;

        try {
            String jsonString = objectMapper.writeValueAsString(orderPayload);
            payload = SdkBytes.fromUtf8String(jsonString);
        } catch (JsonProcessingException e) {
            Logger.error("Failed to convert OrderIotPayload to JSON string.", e);
        }

        return payload;
    }

    private void validateOrderInLiveSight(String inputNamespace, OrderPo order) {
        if (!inputNamespace.equals(order.getNamespace())) {
            throw new OrderApiException(OrderErrorCode._021);
        }
    }
}
