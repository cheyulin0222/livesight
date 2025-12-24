package com.arplanets.corexrapi.livesight.repository.impl.dynamodbsdk;

import com.arplanets.commons.utils.DateTimeConverter;
import com.arplanets.corexrapi.livesight.model.dto.Expiry;
import com.arplanets.corexrapi.livesight.model.eunms.ExpireMode;
import com.arplanets.corexrapi.livesight.model.eunms.PeriodUnit;
import com.arplanets.corexrapi.livesight.model.po.LiveSightPo;
import com.arplanets.corexrapi.livesight.repository.LiveSightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
@Slf4j
public class AwsDynamoDbSdkLiveSightRepository implements LiveSightRepository {

    @Value("${aws.live.sight.table.name}")
    private String tableName;

    private static final String PK_ATTRIBUTE_NAME = "pk";
    private static final String SK_ATTRIBUTE_NAME = "sk";
    private static final String SK_VALUE = "info";

    private final DynamoDbClient dynamoDbClient;

    @Override
    public LiveSightPo create(LiveSightPo liveSight) {

        Map<String, AttributeValue> item = new HashMap<>();
        item.put(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(liveSight.getLiveSightId()).build());
        item.put(SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build());
        item.put("live_sight_id", AttributeValue.builder().s(liveSight.getLiveSightId()).build());
        item.put("org_id", AttributeValue.builder().s(liveSight.getOrgId()).build());
        item.put("created_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(liveSight.getCreatedAt())).build());
        item.put("created_by", AttributeValue.builder().s(liveSight.getCreatedBy()).build());
        item.put("updated_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(liveSight.getUpdatedAt())).build());
        item.put("updated_by", AttributeValue.builder().s(liveSight.getUpdatedBy()).build());

        log.info("Preparing to save item to DynamoDB: {}", formatAttributeValueMap(item));
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression("attribute_not_exists(" + PK_ATTRIBUTE_NAME + ")")
                .build();
        try {
            dynamoDbClient.putItem(putItemRequest);
        } catch (DynamoDbException e) {
            throw new DataAccessResourceFailureException("Failed to added item to DynamoDB", e);
        }

        return liveSight;
    }

    public void createBatch(List<LiveSightPo> liveSights) {
        if (liveSights == null || liveSights.isEmpty()) return;
        if (liveSights.size() > 100) {
            throw new IllegalArgumentException("Transaction cannot exceed 100 items");
        }

        // 建立交易動作清單
        List<TransactWriteItem> actions = liveSights.stream().map(liveSight -> {
            // 1. 準備單筆資料的 Map (跟你的 create 方法一樣)
            Map<String, AttributeValue> item = buildItemMap(liveSight);

            // 2. 封裝成 Put 動作
            Put put = Put.builder()
                    .tableName(tableName)
                    .item(item)
                    .conditionExpression("attribute_not_exists(pk)")
                    .build();

            return TransactWriteItem.builder().put(put).build();
        }).collect(Collectors.toList());

        // 3. 執行交易
        TransactWriteItemsRequest placeOrderTransaction = TransactWriteItemsRequest.builder()
                .transactItems(actions)
                .build();

        try {
            dynamoDbClient.transactWriteItems(placeOrderTransaction);
            log.info("Batch transaction successful for {} items", liveSights.size());
        } catch (TransactionCanceledException e) {
            // 這裡會告訴你為什麼被取消 (例如某一筆 Condition Check 失敗)
            log.error("Transaction cancelled: {}", e.cancellationReasons());
            throw new DataAccessResourceFailureException("DynamoDB Transaction failed and rolled back", e);
        } catch (DynamoDbException e) {
            log.error("DynamoDB error during transaction", e);
            throw new DataAccessResourceFailureException("Failed to execute transaction", e);
        }
    }

    private Map<String, AttributeValue> buildItemMap(LiveSightPo liveSight) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(liveSight.getLiveSightId()).build());
        item.put(SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build());
        item.put("live_sight_id", AttributeValue.builder().s(liveSight.getLiveSightId()).build());
        item.put("org_id", AttributeValue.builder().s(liveSight.getOrgId()).build());
        // ... 其他欄位 (created_at, updated_at 等)
        return item;
    }

    @Override
    public LiveSightPo update(LiveSightPo liveSight) {
        // 檢查 order 物件本身是否為 null
        if (liveSight == null || liveSight.getLiveSightId() == null) {
            throw new IllegalArgumentException("LiveSight and LiveSighId must not be null.");
        }

        StringBuilder updateExpression = new StringBuilder("SET ");
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();

        if (expressionAttributeValues.isEmpty()) {
            return liveSight;
        }

        Optional.ofNullable(liveSight.getUpdatedAt()).ifPresent(s -> {
            updateExpression.append("updated_at = :val_updated_at, ");
            expressionAttributeValues.put(":val_updated_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(s)).build());
        });

        Optional.ofNullable(liveSight.getUpdatedBy()).ifPresent(s -> {
            updateExpression.append("updated_by = :val_updated_by, ");
            expressionAttributeValues.put(":val_updated_by", AttributeValue.builder().s(s).build());
        });

        updateExpression.setLength(updateExpression.length() - 2);

        String conditionExpression = "org_id = :orgId";
        expressionAttributeValues.put(":orgId", AttributeValue.builder().s(liveSight.getOrgId()).build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(liveSight.getLiveSightId()).build(),
                        SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build()))
                .updateExpression(updateExpression.toString())
                .conditionExpression(conditionExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        try {
            UpdateItemResponse response = dynamoDbClient.updateItem(request);
            liveSight = mapToLiveSightPo(response.attributes());
        } catch (DynamoDbException e) {
            throw new DataAccessResourceFailureException("Error updating item in DynamoDB: {}", e);
        }

        return liveSight;


    }

    @Override
    public Optional<LiveSightPo> findById(String liveSightId) {
        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(liveSightId).build(),
                        SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build()))
                .build();


        GetItemResponse response;
        try {
            response = dynamoDbClient.getItem(getItemRequest);
        } catch (DynamoDbException e) {
            throw new DataAccessResourceFailureException("Error fetching item from DynamoDB", e);
        }

        if (response.hasItem()) {
            Map<String, AttributeValue> item = response.item();
            return Optional.of(LiveSightPo.builder()
                    .liveSightId(item.get("live_sight_id") != null ? item.get("live_sight_id").s() : null)
                    .orgId(item.get("org_id") != null ? item.get("org_id").s() : null)
                    .build());
        }

        return Optional.empty();
    }

    private AttributeValue toAttributeValue(Expiry expiry) {
        if (expiry == null) {
            throw new IllegalArgumentException("Expiry configuration cannot be null");
        }

        if (expiry.getExpireMode() == null) {
            throw new IllegalArgumentException("Expire mode must be specified");
        }

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

    private LiveSightPo mapToLiveSightPo(Map<String, AttributeValue> item) {
        return LiveSightPo.builder()
                .liveSightId(Optional.ofNullable(item.get("live_sight_id")).map(AttributeValue::s).orElse(null))
                .orgId(Optional.ofNullable(item.get("org_id")).map(AttributeValue::s).orElse(null))
                .createdBy(item.get("created_by") != null ? item.get("created_by").s() : null )
                .createdAt(item.get("updated_at") != null ? DateTimeConverter.fromFormattedString(item.get("created_at").s()) : null)
                .updatedBy(item.get("updated_by") != null ? item.get("updated_by").s() : null )
                .updatedAt(item.get("updated_at") != null ? DateTimeConverter.fromFormattedString(item.get("updated_at").s()) : null)
                .build();
    }

    private String formatAttributeValueMap(Map<String, AttributeValue> item) {
        return item.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().toString())
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
