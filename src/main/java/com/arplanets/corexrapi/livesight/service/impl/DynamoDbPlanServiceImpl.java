package com.arplanets.corexrapi.livesight.service.impl;

import com.arplanets.corexrapi.livesight.mapper.PlanMapper;
import com.arplanets.corexrapi.livesight.model.dto.PlanDto;
import com.arplanets.corexrapi.livesight.model.dto.req.PlanCreateRequest;
import com.arplanets.corexrapi.livesight.model.dto.req.PlanUpdateRequest;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanBatchCreateResponse;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanBatchFailure;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanBatchUpdateFailedResponse;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanBatchUpdateResponse;
import com.arplanets.corexrapi.livesight.model.po.PlanPo;
import com.arplanets.corexrapi.livesight.repository.PlanRepository;
import com.arplanets.corexrapi.livesight.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbPlanServiceImpl implements PlanService {

    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Taipei");

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;
    private static final String PLANS_FROM_LIVE_SIGHT_CACHE = "planFromLiveSight";


    @Override
    public PlanBatchCreateResponse batchCreatePlan(List<PlanCreateRequest> request, String liveSightId, String uuid) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);

        // 1. 準備所有要寫入的 PO
        List<PlanPo> plans = request.stream()
                .map(p -> buildCreatePlane(p, liveSightId, uuid, now))
                .toList();

        // 2. 執行大批次交易寫入
        List<PlanPo> failedInTransaction = planRepository.createWithTransaction(plans);

        // 計算初步成功的項目 (總數 - 交易失敗數)
        Set<String> failedIds = failedInTransaction.stream()
                .map(PlanPo::getPlanId)
                .collect(Collectors.toSet());

        List<PlanPo> successItems = plans.stream()
                .filter(po -> !failedIds.contains(po.getPlanId()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<PlanBatchFailure> failedItems = new ArrayList<>();

        // 3. 降級邏輯：針對交易失敗的項目進行單筆寫入，以識別具體錯誤原因
        for (PlanPo plan : failedInTransaction) {
            try {
                planRepository.create(plan);
                successItems.add(plan);
            } catch (ConditionalCheckFailedException e) {
                failedItems.add(
                        PlanBatchFailure.builder()
                                .planName(plan.getPlanName())
                                .expiry(plan.getExpiry())
                                .reason("PK/SK 組合已存在 (重複建立)")
                                .build());
            } catch (Exception e) {
                failedItems.add(
                        PlanBatchFailure.builder()
                                .planName(plan.getPlanName())
                                .expiry(plan.getExpiry())
                                .reason("寫入失敗: " + e.getMessage())
                                .build());
            }
        }

        // 4. 封裝最終 Response
        return PlanBatchCreateResponse.builder()
                .liveSightId(liveSightId)
                .successItems(successItems.stream().map(planMapper::planPoToPlanCreateResponse).toList())
                .failedItems(failedItems)
                .totalCount(plans.size())
                .successCount(successItems.size())
                .failedCount(failedItems.size())
                .allSuccess(failedItems.isEmpty())
                .build();
    }

    @Override
    public PlanBatchUpdateResponse batchUpdatePlan(List<PlanUpdateRequest> request, String liveSightId, String user) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);

        List<PlanPo> plans = request.stream().map(plan -> buildUpdatePlane(plan, liveSightId, user, now)).toList();

        List<PlanPo> failedInTransaction = planRepository.updateWithTransaction(plans);

        Set<String> failedIds = failedInTransaction.stream()
                .map(PlanPo::getPlanId)
                .collect(Collectors.toSet());

        List<PlanPo> successItems = plans.stream()
                .filter(plan -> !failedIds.contains(plan.getPlanId()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<PlanBatchUpdateFailedResponse> failedItems = new ArrayList<>();

        for (PlanPo plan : failedInTransaction) {
            try {
                planRepository.update(plan);
                successItems.add(plan);
            } catch (ConditionalCheckFailedException e) {
                failedItems.add(
                        PlanBatchUpdateFailedResponse.builder()
                                .planId(plan.getPlanId())
                                .reason("PK/SK 組合不存在")
                                .build());
            } catch (Exception e) {
                failedItems.add(
                        PlanBatchUpdateFailedResponse.builder()
                                .planId(plan.getPlanId())
                                .reason("寫入失敗: " + e.getMessage())
                                .build());
            }
        }

        return PlanBatchUpdateResponse.builder()
                .liveSightId(liveSightId)
                .successItems(successItems.stream().map(planMapper::planPoToPlanUpdateResponse).toList())
                .failedItems(failedItems)
                .totalCount(plans.size())
                .successCount(successItems.size())
                .failedCount(failedItems.size())
                .allSuccess(failedItems.isEmpty())
                .build();
    }

    @Cacheable(value = PLANS_FROM_LIVE_SIGHT_CACHE, key="#liveSightId")
    @Override
    public Map<String, PlanDto> findByLiveSightId(String liveSightId) {
        List<PlanPo> result = planRepository.listByLiveSightId(liveSightId);

        Map<String, PlanDto> planMap = new HashMap<>(result.size() + 1);

        for (PlanPo plan : result) {
            PlanDto dto = planMapper.planePoToPlaneDto(plan);

            planMap.put(dto.getPlanId(), dto);

            if (Boolean.TRUE.equals(dto.getStandard())) {
                planMap.put("standard", dto);
            }
        }

        return planMap;
    }

    private PlanPo buildCreatePlane(PlanCreateRequest plan, String liveSightId, String user, ZonedDateTime now) {
        return PlanPo.builder()
                .planId(UUID.randomUUID().toString())
                .planName(plan.getPlanName())
                .liveSightId(liveSightId)
                .expiry(plan.getExpiry())
                .standard(plan.getStandard())
                .createdBy(user)
                .createdAt(now)
                .updatedBy(user)
                .updatedAt(now)
                .build();
    }

    private PlanPo buildUpdatePlane(PlanUpdateRequest plane, String liveSightId, String user, ZonedDateTime now) {
        return PlanPo.builder()
                .planId(plane.getPlanId())
                .planName(plane.getPlanName())
                .liveSightId(liveSightId)
                .expiry(plane.getExpiry())
                .updatedBy(user)
                .updatedAt(now)
                .build();
    }
}
