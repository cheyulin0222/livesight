package com.arplanets.corexrapi.livesight.repository.impl.dynamodbsdk;

import com.arplanets.commons.utils.DateTimeConverter;
import com.arplanets.corexrapi.livesight.model.dto.Expiry;
import com.arplanets.corexrapi.livesight.model.eunms.ExpireMode;
import com.arplanets.corexrapi.livesight.model.eunms.PeriodUnit;
import com.arplanets.corexrapi.livesight.model.po.PlanPo;
import com.arplanets.corexrapi.livesight.repository.PlanRepository;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AwsDynamoDbSdkPlanRepositoryImpl implements PlanRepository {

    @Value("${aws.plan.table.name}")
    private String tableName;

    private static final String PK_ATTRIBUTE_NAME = "pk";
    private static final String SK_ATTRIBUTE_NAME = "sk";
    private static final String SK_VALUE = "info";

    private final DynamoDbClient dynamoDbClient;

    @Override
    public PlanPo create(PlanPo plan) {
        if (plan == null) {
            throw new DataAccessResourceFailureException("No item to be created.");
        }

        try {
            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(buildItemMap(plan))
                    .conditionExpression("attribute_not_exists(#pk)")
                    .expressionAttributeNames(Map.of("#pk", PK_ATTRIBUTE_NAME))
                    .build());
            return plan;
        } catch (ConditionalCheckFailedException e) {
            // 單筆寫入時，若 PK/SK 重複則拋出特定異常，讓 Service 捕捉
            throw e;
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("DynamoDB 單筆寫入失敗: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PlanPo> createWithTransaction(List<PlanPo> plans) {
        if (CollectionUtils.isEmpty(plans)) {
            throw new DataAccessResourceFailureException("No item to be created.");
        }

        List<PlanPo> failedPartitions = new ArrayList<>();

        // 每 100 筆為一個 Transaction 分區
        Lists.partition(plans, 100).forEach(partition -> {
            try {
                List<TransactWriteItem> actions = partition.stream()
                        .map(plan -> TransactWriteItem.builder()
                                .put(Put.builder()
                                        .tableName(tableName)
                                        .item(buildItemMap(plan))
                                        .conditionExpression("attribute_not_exists(#pk)")
                                        .expressionAttributeNames(Map.of("#pk", PK_ATTRIBUTE_NAME))
                                        .build())
                                .build())
                        .toList();

                dynamoDbClient.transactWriteItems(TransactWriteItemsRequest.builder()
                        .transactItems(actions)
                        .build());
            } catch (TransactionCanceledException e) {
                log.warn("Transaction 分區寫入失敗，原因: {}，將由 Service 進行降級檢查", e.getMessage());
                failedPartitions.addAll(partition);
            } catch (Exception e) {
                log.error("Transaction 發生非預期錯誤", e);
                failedPartitions.addAll(partition);
            }
        });

        return failedPartitions; // 回傳所有在 Transaction 中沒能成功寫入的 PO
    }

    @Override
    public PlanPo update(PlanPo plan) {
        if (plan == null) {
            throw new DataAccessResourceFailureException("No item to be updated.");
        }

        StringBuilder updateExpression = new StringBuilder("SET ");
        Map<String, String> expressionAttributeNames = new HashMap<>();
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();

        Optional.ofNullable(plan.getPlanName()).ifPresent(s -> {
            updateExpression.append("#plan_name = :val_plan_name, ");
            expressionAttributeNames.put("#plan_name", "plan_name");
            expressionAttributeValues.put(":val_plan_name", AttributeValue.builder().s(s).build());
        });

        if (plan.getExpiry() != null) {
            updateExpression.append("#expiry = :val_expiry, ");
            expressionAttributeNames.put("#expiry", "expiry");
            expressionAttributeValues.put(":val_plan_name", getExpiry(plan.getExpiry()));
            expressionAttributeValues.put(":val_expiry", getExpiry(plan.getExpiry()));
        }

        if (expressionAttributeValues.isEmpty()) {
            throw new DataAccessResourceFailureException("No item to be updated.");
        }

        Optional.ofNullable(plan.getUpdatedAt()).ifPresent(s -> {
            updateExpression.append("updated_at = :val_updated_at, ");
            expressionAttributeValues.put(":val_updated_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(s)).build());
        });

        Optional.ofNullable(plan.getUpdatedBy()).ifPresent(s -> {
            updateExpression.append("updated_by = :val_updated_by, ");
            expressionAttributeValues.put(":val_updated_by", AttributeValue.builder().s(s).build());
        });

        updateExpression.setLength(updateExpression.length() - 2);

        String conditionExpression = "#live_sight_id=:val_live_sight_id";
        expressionAttributeNames.put("#live_sight_id", "live_sight_id");
        expressionAttributeValues.put(":orgId", AttributeValue.builder().s(plan.getLiveSightId()).build());

        try {
            UpdateItemResponse response = dynamoDbClient.updateItem(
                    UpdateItemRequest.builder()
                            .tableName(tableName)
                            .key(Map.of(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(plan.getPlanId()).build(),
                                    SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build()))
                            .updateExpression(updateExpression.toString())
                            .conditionExpression(conditionExpression)
                            .expressionAttributeNames(expressionAttributeNames)
                            .expressionAttributeValues(expressionAttributeValues)
                            .returnValues(ReturnValue.ALL_NEW)
                            .build());

            plan = mapToPlanPo(response.attributes());
        } catch (DynamoDbException e) {
            throw new DataAccessResourceFailureException("Error updating item in DynamoDB: {}", e);
        }

        return plan;
    }

    @Override
    public List<PlanPo> updateWithTransaction(List<PlanPo> plans) {
        if (CollectionUtils.isEmpty(plans)) {
            throw new DataAccessResourceFailureException("No item to be updated.");
        }

        List<PlanPo> failedPartitions = new ArrayList<>();

        // 每 100 筆為一個 Transaction 分區
        Lists.partition(plans, 100).forEach(partition -> {
            try {
                List<TransactWriteItem> actions = partition.stream()
                        .map(plan -> TransactWriteItem.builder()
                                .update(buildUpdateAction(plan))
                                .build())
                        .toList();

                dynamoDbClient.transactWriteItems(TransactWriteItemsRequest.builder()
                        .transactItems(actions)
                        .build());
            } catch (TransactionCanceledException e) {
                log.warn("Transaction 分區寫入失敗，原因: {}，將由 Service 進行降級檢查", e.getMessage());
                failedPartitions.addAll(partition);
            } catch (Exception e) {
                log.error("Transaction 發生非預期錯誤", e);
                failedPartitions.addAll(partition);
            }
        });
        return failedPartitions;
    }

    // 暫時!!!!!!!!!!!!!!!!!
    @Override
    public List<PlanPo> listByLiveSightId(String liveSightId) {
        Map<String, String> expressionAttributeNames = new HashMap<>();
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();

        // 將所有過濾條件放入 FilterExpression
        String filterExpression = "#live_sight_id = :val_live_sight_id AND #is_active = :val_active AND attribute_not_exists(#deleted_at)";

        expressionAttributeNames.put("#live_sight_id", "live_sight_id");
        expressionAttributeNames.put("#is_active", "is_active");
        expressionAttributeNames.put("#deleted_at", "deleted_at");

        expressionAttributeValues.put(":val_live_sight_id", AttributeValue.builder().s(liveSightId).build());
        expressionAttributeValues.put(":val_active", AttributeValue.builder().n("1").build());

        // 使用 scan 掃描全表並過濾
        ScanResponse response = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(tableName)
                .filterExpression(filterExpression)
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build());

        return response.items().stream()
                .map(this::mapToPlanPo)
                .collect(Collectors.toList());
    }

//    @Override
//    public List<PlanPo> listByLiveSightId(String liveSightId) {
//        Map<String, String> expressionAttributeNames = new HashMap<>();
//        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
//
//        String keyConditionExpression = "#live_sight_id = :val_live_sight_id";
//
//        String filterExpression = "#is_active = :val_active AND attribute_not_exists(#deleted_at)";
//
//        expressionAttributeNames.put("#live_sight_id", "live_sight_id");
//        expressionAttributeNames.put("#is_active", "is_active");
//        expressionAttributeNames.put("#deleted_at", "deleted_at");
//
//        expressionAttributeValues.put(":val_live_sight_id", AttributeValue.builder().s(liveSightId).build());
//        expressionAttributeValues.put(":val_active", AttributeValue.builder().n("1").build());
//
//
//
//        QueryResponse response = sendQuery(QueryRequest.builder()
//                .tableName(tableName)
//                .indexName("live_sight_id-created_at-index")
//                .keyConditionExpression(keyConditionExpression)
//                .filterExpression(filterExpression)
//                .expressionAttributeNames(expressionAttributeNames)
//                .expressionAttributeValues(expressionAttributeValues)
//                .scanIndexForward(false)
//                .build());
//
//        return response.items().stream()
//                .map(this::mapToPlanPo)
//                .collect(Collectors.toList());
//    }


    @Override
    public Optional<PlanPo> findById(String planeId) {
        return Optional.empty();
    }

    private Update buildUpdateAction(PlanPo plan) {
        UpdateSpec spec = generateUpdateSpec(plan);

        return Update.builder()
                .tableName(tableName)
                .key(Map.of(
                        PK_ATTRIBUTE_NAME, AttributeValue.builder().s(plan.getPlanId()).build(),
                        SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build()
                ))
                .updateExpression(spec.updateExpression)
                .conditionExpression(spec.conditionExpression)
                .expressionAttributeNames(spec.expressionAttributeNames)
                .expressionAttributeValues(spec.expressionAttributeValues)
                .build();
    }


    private Map<String, AttributeValue> buildItemMap(PlanPo plan) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(plan.getPlanId()).build());
        item.put(SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build());
        item.put("plan_id", AttributeValue.builder().s(plan.getPlanId()).build());
        item.put("live_sight_id", AttributeValue.builder().s(plan.getLiveSightId()).build());
        item.put("is_active", AttributeValue.builder().n("1").build());
        item.put("created_by", AttributeValue.builder().s(plan.getCreatedBy()).build());
        item.put("created_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(plan.getCreatedAt())).build());
        item.put("updated_by", AttributeValue.builder().s(plan.getUpdatedBy()).build());
        item.put("updated_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(plan.getUpdatedAt())).build());


        if (StringUtils.hasText(plan.getPlanName())) {
            item.put("plan_name", AttributeValue.builder().s(plan.getPlanName()).build());
        }

        AttributeValue expiry = getExpiry(plan.getExpiry());
        if (expiry != null) {
            item.put("expiry", expiry);
        }

        if (plan.getStandard()) {
            item.put("is_standard", AttributeValue.builder().n("1").build());
        } else {
            item.put("is_standard", AttributeValue.builder().n("0").build());
        }

        return item;

    }

    private AttributeValue getExpiry(Expiry expiry) {
        if (expiry != null && expiry.getExpireMode() != null) {
            Map<String, AttributeValue> map = new HashMap<>();
            ExpireMode mode = expiry.getExpireMode();
            map.put("expire_mode", AttributeValue.builder().s(mode.name()).build());
            switch (mode) {
                case RELATIVE:
                    if (expiry.getDuration() == null) {
                        throw new IllegalArgumentException("Duration is required for RELATIVE mode");
                    }
                    map.put("duration", AttributeValue.builder().n(String.valueOf(expiry.getDuration())).build());
                    break;

                case PERIOD_ALIGNED:
                    if (expiry.getPeriodUnit() == null) {
                        throw new IllegalArgumentException("Period unit is required for PERIOD_ALIGNED mode");
                    }
                    map.put("period_unit", AttributeValue.builder().s(expiry.getPeriodUnit().name()).build());
                    break;

                case ABSOLUTE:
                    if (expiry.getFixedAt() == null) {
                        throw new IllegalArgumentException("Fixed date (fixed_at) is required for ABSOLUTE mode");
                    }
                    map.put("fixed_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(expiry.getFixedAt())).build());
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported expire mode: " + mode);
            }

            return AttributeValue.builder().m(map).build();
        }
        return null;
    }

    private PlanPo mapToPlanPo(Map<String, AttributeValue> item) {
        return PlanPo.builder()
                .planId(Optional.ofNullable(item.get("plan_id")).map(AttributeValue::s).orElse(null))
                .planName(Optional.ofNullable(item.get("plan_name")).map(AttributeValue::s).orElse(null))
                .liveSightId(Optional.ofNullable(item.get("live_sight_id")).map(AttributeValue::s).orElse(null))
                .expiry(fromAttributeValue(item.get("expiry")))
                .standard(Optional.ofNullable(item.get("is_standard"))
                        .map(attr -> "1".equals(attr.n()))
                        .orElse(false))
                .createdBy(item.get("created_by") != null ? item.get("created_by").s() : null )
                .createdAt(item.get("created_at") != null ? DateTimeConverter.fromFormattedString(item.get("created_at").s()) : null)
                .updatedBy(item.get("updated_by") != null ? item.get("updated_by").s() : null )
                .updatedAt(item.get("updated_at") != null ? DateTimeConverter.fromFormattedString(item.get("updated_at").s()) : null)
                .build();
    }

    private Expiry fromAttributeValue(AttributeValue av) {
        if (av == null || av.m() == null || av.m().isEmpty()) {
            return null;
        }

        Map<String, AttributeValue> m = av.m();

        return Expiry.builder()
                .expireMode(m.containsKey("expire_mode") ? ExpireMode.valueOf(m.get("expire_mode").s()) : null)
                .duration(m.containsKey("duration") ? Long.parseLong(m.get("duration").n()) : null)
                .periodUnit(m.containsKey("period_unit") ? PeriodUnit.valueOf(m.get("period_unit").s()) : null )
                .fixedAt(m.containsKey("fixed_at") ? DateTimeConverter.fromFormattedString(m.get("fixed_at").s()) : null)
                .build();

    }

    private UpdateSpec generateUpdateSpec(PlanPo plan) {
        StringBuilder updateExpression = new StringBuilder("SET ");
        Map<String, String> names = new HashMap<>();
        Map<String, AttributeValue> values = new HashMap<>();

        // 1. 處理 Plan Name
        Optional.ofNullable(plan.getPlanName()).ifPresent(s -> {
            updateExpression.append("#plan_name = :val_plan_name, ");
            names.put("#plan_name", "plan_name");
            values.put(":val_plan_name", AttributeValue.builder().s(s).build());
        });

        // 2. 處理 Expiry (修正了你原本多放一個 :val_plan_name 的問題)
        if (plan.getExpiry() != null) {
            updateExpression.append("#expiry = :val_expiry, ");
            names.put("#expiry", "expiry");
            values.put(":val_expiry", getExpiry(plan.getExpiry()));
        }

        // 3. 處理審計欄位
        Optional.ofNullable(plan.getUpdatedAt()).ifPresent(s -> {
            updateExpression.append("updated_at = :val_updated_at, ");
            values.put(":val_updated_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(s)).build());
        });

        Optional.ofNullable(plan.getUpdatedBy()).ifPresent(s -> {
            updateExpression.append("updated_by = :val_updated_by, ");
            values.put(":val_updated_by", AttributeValue.builder().s(s).build());
        });

        updateExpression.setLength(updateExpression.length() - 2);

        // 4. Condition 邏輯 (修正了你原本 :orgId 對不上的問題)
        String conditionExpression = "#live_sight_id = :val_live_sight_id";
        names.put("#live_sight_id", "live_sight_id");
        values.put(":val_live_sight_id", AttributeValue.builder().s(plan.getLiveSightId()).build());

        return new UpdateSpec(updateExpression.toString(), conditionExpression, names, values);
    }

    private QueryResponse sendQuery(QueryRequest request) {
        try {
            return dynamoDbClient.query(request);
        } catch (DynamoDbException e) {
            log.error("查詢 DynamoDB 失敗: {}", e.getMessage());
            throw new RuntimeException("查詢訂單列表失敗。", e);
        }
    }



    private record UpdateSpec(String updateExpression, String conditionExpression,
                              Map<String, String> expressionAttributeNames,
                              Map<String, AttributeValue> expressionAttributeValues) {}
}
