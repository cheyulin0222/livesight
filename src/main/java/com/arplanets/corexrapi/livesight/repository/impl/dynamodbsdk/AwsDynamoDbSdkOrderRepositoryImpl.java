package com.arplanets.corexrapi.livesight.repository.impl.dynamodbsdk;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.enums.OrderErrorCode;
import com.arplanets.corexrapi.livesight.model.dto.res.PageResult;
import com.arplanets.corexrapi.livesight.model.eunms.OrderStatus;
import com.arplanets.corexrapi.livesight.model.po.OrderPo;
import com.arplanets.corexrapi.livesight.repository.OrderRepository;
import com.arplanets.commons.utils.DateTimeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AwsDynamoDbSdkOrderRepositoryImpl implements OrderRepository {

    @Value("${aws.live.sight.authorization.table.name}")
    private String tableName;

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;

    private static final String PK_ATTRIBUTE_NAME = "pk";
    private static final String SK_ATTRIBUTE_NAME = "sk";
    private static final String SK_VALUE = "info";

    @Override
    public OrderPo create(OrderPo order) {

        Map<String, AttributeValue> item = new HashMap<>();
        // id
        item.put(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(order.getOrderId()).build());
        item.put(SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build());
        // 專案資訊
        item.put("order_id", AttributeValue.builder().s(order.getOrderId()).build());
        item.put("namespace", AttributeValue.builder().s(order.getNamespace()).build());
        item.put("product_id", AttributeValue.builder().s(order.getProductId()).build());
        item.put("service_type", AttributeValue.builder().s(order.getServiceType()).build());
        item.put("service_type_id", AttributeValue.builder().s(order.getServiceTypeId()).build());
        // 訂單狀態
        item.put("order_status", AttributeValue.builder().s(order.getOrderStatus().name()).build());
        // 使用者資訊
        item.put("auth_type", AttributeValue.builder().s(order.getAuthType()).build());
        item.put("auth_type_id", AttributeValue.builder().s(order.getAuthTypeId()).build());
        item.put("user_browser", AttributeValue.builder().s(order.getUserBrowser()).build());
        item.put("user_device_type", AttributeValue.builder().s(order.getUserDeviceType()).build());
        item.put("user_OS", AttributeValue.builder().s(order.getUserOs()).build());
        // 訂單建立資訊
        item.put("created_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getCreatedAt())).build());
        item.put("verification_code", AttributeValue.builder().s(order.getVerificationCode()).build());
        // 過期資訊
        item.put("expired_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getExpiredAt())).build());
        // 更新資訊
        item.put("updated_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getUpdatedAt())).build());
        // ttl 資訊
        item.put("TTL", AttributeValue.builder().n(String.valueOf(order.getTtl().toEpochSecond())).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();
        try {
            dynamoDbClient.putItem(putItemRequest);
        } catch (DynamoDbException e) {
            throw new DataAccessResourceFailureException("Failed to added item to DynamoDB", e);
        }

        return order;
    }

    @Override
    public Optional<OrderPo> findById(String orderId) {

        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(orderId).build(),
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
            return Optional.of(OrderPo.builder()
                    .orderId(item.get("order_id") != null ? item.get("order_id").s() : null)
                    .orderStatus(item.get("order_status") != null ? OrderStatus.toOrderStatus(item.get("order_status").s()) : null)
                    .namespace(item.get("namespace") != null ? item.get("namespace").s() : null)
                    .productId(item.get("product_id") != null ? item.get("product_id").s() : null)
                    .serviceType(item.get("service_type") != null ? item.get("service_type").s() : null)
                    .serviceTypeId(item.get("service_type_id") != null ? item.get("service_type_id").s() : null)
                    .authType(item.get("auth_type") != null ? item.get("auth_type").s() : null)
                    .authTypeId(item.get("auth_type_id") != null ? item.get("auth_type_id").s() : null)
                    .userBrowser(item.get("user_browser") != null ? item.get("user_browser").s() : null)
                    .userOs(item.get("user_OS") != null ? item.get("user_OS").s() : null)
                    .userDeviceType(item.get("user_device_type") != null ? item.get("user_device_type").s() : null)
                    .createdAt(item.get("created_at") != null ? DateTimeConverter.fromFormattedString(item.get("created_at").s()) : null)
                    .verificationCode(item.get("verification_code") != null ? item.get("verification_code").s() : null)
                    .activatedAt(item.get("activated_at") != null ? DateTimeConverter.fromFormattedString(item.get("activated_at").s()) : null)
                    .activatedBy(item.get("activated_by") != null ? item.get("activated_by").s() : null)
                    .redeemCode(item.get("redeem_code") != null ? item.get("redeem_code").s() : null)
                    .redeemedAt(item.get("redeemed_at") != null ? DateTimeConverter.fromFormattedString(item.get("redeemed_at").s()) : null)
                    .accessToken(item.get("access_token") != null ? item.get("access_token").s() : null)
                    .voidedAt(item.get("voided_at") != null ? DateTimeConverter.fromFormattedString(item.get("voided_at").s()) : null)
                    .voidedBy(item.get("voided_by") != null ? item.get("voided_by").s() : null)
                    .returnedAt(item.get("returned_at") != null ? DateTimeConverter.fromFormattedString(item.get("returned_at").s()) : null)
                    .returnedBy(item.get("returned_by") != null ? item.get("returned_by").s() : null)
                    .expiredAt(item.get("expired_at") != null ? DateTimeConverter.fromFormattedString(item.get("expired_at").s()) : null)
                    .updatedAt(item.get("updated_at") != null ? DateTimeConverter.fromFormattedString(item.get("updated_at").s()) : null)
                    .ttl(item.get("TTL") != null ? DateTimeConverter.fromEpochSecondToZonedDateTime(item.get("TTL").n()) : null)
                    .build());
        }

        return Optional.empty();
    }

    @Override
    public OrderPo update(OrderPo order) {
        // 檢查 order 物件本身是否為 null
        if (order == null || order.getOrderId() == null) {
            throw new IllegalArgumentException("Order and OrderId must not be null.");
        }

        Map<String, AttributeValueUpdate> item = new HashMap<>();

        Optional.ofNullable(order.getOrderStatus()).ifPresent(s ->
            item.put("order_status", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(order.getOrderStatus().name()).build())
                    .action(AttributeAction.PUT)
                        .build())
        );

        Optional.ofNullable(order.getVerificationCode()).ifPresent(s ->
            item.put("verification_code", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(order.getVerificationCode()).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getActivatedAt()).ifPresent(s ->
            item.put("activated_at", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getActivatedAt())).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getActivatedBy()).ifPresent(s ->
            item.put("activated_by", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(order.getActivatedBy()).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getRedeemCode()).ifPresent(s ->
            item.put("redeem_code", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(order.getRedeemCode()).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getRedeemedAt()).ifPresent(s ->
            item.put("redeemed_at", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getRedeemedAt())).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getAccessToken()).ifPresent(s ->
            item.put("access_token", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(order.getAccessToken()).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getVoidedAt()).ifPresent(s ->
            item.put("voided_at", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getVoidedAt())).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getVoidedBy()).ifPresent(s ->
            item.put("voided_by", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(order.getVoidedBy()).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getReturnedAt()).ifPresent(s ->
            item.put("returned_at", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getReturnedAt())).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getReturnedBy()).ifPresent(s ->
            item.put("returned_by", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(order.getReturnedBy()).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getExpiredAt()).ifPresent(s ->
            item.put("expired_at", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getExpiredAt())).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        Optional.ofNullable(order.getUpdatedAt()).ifPresent(s ->
            item.put("updated_at", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getUpdatedAt())).build())
                    .action(AttributeAction.PUT)
                    .build())
        );

        String conditionExpression = null;
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        if (order.getOrderStatus() == OrderStatus.VOIDED) {
            conditionExpression = "order_status != :expectedStatus AND product_id = :expectedProductId AND attribute_exists(orderId)";
            // 驗證 status
            expressionAttributeValues.put(":expectedStatus", AttributeValue.builder().s(OrderStatus.VOIDED.name()).build());
            // 驗證 ProductId
            expressionAttributeValues.put(":expectedProductId", AttributeValue.builder().s(order.getProductId()).build());
        } else if (order.getOrderStatus() == OrderStatus.ACTIVATED) {
            conditionExpression = "order_status = :expectedStatus AND product_id = :expectedProductId AND expired_at > :now AND attribute_exists(orderId)";
            // 驗證 status
            expressionAttributeValues.put(":expectedStatus", AttributeValue.builder().s(OrderStatus.PENDING.name()).build());
            // 驗證 Product ID
            expressionAttributeValues.put(":expectedProductId", AttributeValue.builder().s(order.getProductId()).build());
            // 驗證效期
            expressionAttributeValues.put(":now", AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getUpdatedAt())).build());
        } else if (order.getOrderStatus() == OrderStatus.REDEEMED) {
            conditionExpression = "order_status = :expectedStatus AND expired_at > :now AND attribute_exists(orderId) AND redeem_code = :redeemCode AND product_id = :expectedProductId";
            // 驗證 Redeem Code
            expressionAttributeValues.put(":redeemCode", AttributeValue.builder().s(order.getRedeemCode()).build());
            // 驗證 status
            expressionAttributeValues.put(":expectedStatus", AttributeValue.builder().s(OrderStatus.ACTIVATED.name()).build());
            // 驗證 Product ID
            expressionAttributeValues.put(":expectedProductId", AttributeValue.builder().s(order.getProductId()).build());
            // 驗證效期
            expressionAttributeValues.put(":now", AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getUpdatedAt())).build());
        } else if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            conditionExpression = "order_status = :expectedStatus AND product_id = :expectedProductId AND attribute_exists(orderId)";
            // 驗證 status
            expressionAttributeValues.put(":expectedStatus", AttributeValue.builder().s(OrderStatus.REDEEMED.name()).build());
            // 驗證 ProductId
            expressionAttributeValues.put(":expectedProductId", AttributeValue.builder().s(order.getProductId()).build());
        }

        // 檢查 expressionAttributeValues 的值是否為 null，避免 AWS SDK 報錯
        for (Map.Entry<String, AttributeValue> entry : expressionAttributeValues.entrySet()) {
            if (entry.getValue() == null || entry.getValue().s() == null) {
                throw new IllegalArgumentException("Expression attribute value for " + entry.getKey() + " cannot be null.");
            }
        }

        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(order.getOrderId()).build(),
                        SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build()))
                .attributeUpdates(item)
                .conditionExpression(conditionExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        try {
            dynamoDbClient.updateItem(updateItemRequest);
        } catch (ConditionalCheckFailedException e) {
            if (order.getOrderStatus() == OrderStatus.VOIDED) {
                throw new OrderApiException(OrderErrorCode._016);
            } else if (order.getOrderStatus() == OrderStatus.ACTIVATED) {
                throw new OrderApiException(OrderErrorCode._017);
            } else if (order.getOrderStatus() == OrderStatus.REDEEMED) {
                throw new OrderApiException(OrderErrorCode._018);
            } else if (order.getOrderStatus() == OrderStatus.COMPLETED) {
                throw new OrderApiException(OrderErrorCode._019);
            } else {
                throw new OrderApiException(OrderErrorCode._020);
            }
        } catch (DynamoDbException e) {
            throw new DataAccessResourceFailureException("Error updating item in DynamoDB: {}", e);
        }

        return order;
    }

    @Override
    public PageResult<OrderPo> pageByServiceTypeId(String serviceTypeId, ZonedDateTime startDate, ZonedDateTime endDate, Integer pageSize, String lastEvaluatedKey) {
        QueryRequest.Builder requestBuilder = QueryRequest.builder()
                .tableName(tableName)
                .indexName("service_type_id-created_at-index")
                .keyConditionExpression("service_type_id = :service_type_id AND created_at BETWEEN :start_date AND :end_date")
                .expressionAttributeValues(Map.of(
                        ":service_type_id", AttributeValue.builder().s(serviceTypeId).build(),
                        ":start_date", AttributeValue.builder().s(DateTimeConverter.toFormattedString(startDate)).build(),
                        ":end_date", AttributeValue.builder().s(DateTimeConverter.toFormattedString(endDate)).build()
                ))
                // 取多一筆數量，為了判斷有沒有下一筆
                .limit(pageSize + 1);


        Map<String, AttributeValue> lastEvaluatedKeyMap = deserializeKey(lastEvaluatedKey);

        if (lastEvaluatedKeyMap != null && !lastEvaluatedKeyMap.isEmpty()) {
            requestBuilder.exclusiveStartKey(lastEvaluatedKeyMap);
        }

        QueryResponse response;
        try {
            response = dynamoDbClient.query(requestBuilder.build());
        } catch (DynamoDbException e) {
            log.error("查詢 DynamoDB 失敗: {}", e.getMessage());
            throw new RuntimeException("查詢訂單列表失敗。", e);
        }

        // 4. 處理查詢結果
        List<OrderPo> orders = response.items().stream()
                .map(this::mapToOrderPo)
                .collect(Collectors.toList());

        // 如果回傳數量 > 原始查詢數量 就是有下一筆
        boolean hasNextPage = orders.size() > pageSize;

        String nextExclusiveStartKey = null;

        if (hasNextPage) {
            // 取得原始最後一筆資料的 key
            Map<String, AttributeValue> nextExclusiveStartKeyMap = getKey(orders.get(pageSize - 1));
            nextExclusiveStartKey  = serializeKey(nextExclusiveStartKeyMap);

            // 將多出來的項目移除，只回傳 pageSize 筆
            orders = orders.subList(0, pageSize);
        }

        // 5. 回傳分頁結果
        return new PageResult<>(orders, nextExclusiveStartKey, hasNextPage);
    }

    private OrderPo mapToOrderPo(Map<String, AttributeValue> item) {
        return OrderPo.builder()
                .orderId(Optional.ofNullable(item.get("order_id")).map(AttributeValue::s).orElse(null))
                .namespace(Optional.ofNullable(item.get("namespace")).map(AttributeValue::s).orElse(null))
                .productId(Optional.ofNullable(item.get("product_id")).map(AttributeValue::s).orElse(null))
                .serviceType(Optional.ofNullable(item.get("service_type")).map(AttributeValue::s).orElse(null))
                .serviceTypeId(Optional.ofNullable(item.get("service_type_id")).map(AttributeValue::s).orElse(null))
                .authType(Optional.ofNullable(item.get("auth_type")).map(AttributeValue::s).orElse(null))
                .authTypeId(Optional.ofNullable(item.get("auth_type_id")).map(AttributeValue::s).orElse(null))
                .userBrowser(Optional.ofNullable(item.get("user_browser")).map(AttributeValue::s).orElse(null))
                .userOs(Optional.ofNullable(item.get("user_OS")).map(AttributeValue::s).orElse(null))
                .userDeviceType(Optional.ofNullable(item.get("user_device_type")).map(AttributeValue::s).orElse(null))
                .orderStatus(item.get("order_status") != null ? OrderStatus.toOrderStatus(item.get("order_status").s()) : null)
                .createdAt(item.get("created_at") != null ? DateTimeConverter.fromFormattedString(item.get("created_at").s()) : null)
                .verificationCode(item.get("verification_code") != null ? item.get("verification_code").s() : null)
                .activatedAt(item.get("activated_at") != null ? DateTimeConverter.fromFormattedString(item.get("activated_at").s()) : null)
                .activatedBy(item.get("activated_by") != null ? item.get("activated_by").s() : null)
                .redeemCode(item.get("redeem_code") != null ? item.get("redeem_code").s() : null)
                .redeemedAt(item.get("redeemed_at") != null ? DateTimeConverter.fromFormattedString(item.get("redeemed_at").s()) : null)
                .accessToken(item.get("access_token") != null ? item.get("access_token").s() : null)
                .voidedAt(item.get("voided_at") != null ? DateTimeConverter.fromFormattedString(item.get("voided_at").s()) : null)
                .voidedBy(item.get("voided_by") != null ? item.get("voided_by").s() : null)
                .returnedAt(item.get("returned_at") != null ? DateTimeConverter.fromFormattedString(item.get("returned_at").s()) : null)
                .returnedBy(item.get("returned_by") != null ? item.get("returned_by").s() : null)
                .expiredAt(item.get("expired_at") != null ? DateTimeConverter.fromFormattedString(item.get("expired_at").s()) : null)
                .updatedAt(item.get("updated_at") != null ? DateTimeConverter.fromFormattedString(item.get("updated_at").s()) : null)
                .ttl(item.get("TTL") != null ? DateTimeConverter.fromEpochSecondToZonedDateTime(item.get("TTL").n()) : null)
                .build();
    }

    private Map<String, AttributeValue> deserializeKey(String lastEvaluatedKey) {
        if (!StringUtils.hasText(lastEvaluatedKey)) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(lastEvaluatedKey);
            String jsonKey = new String(decodedBytes, StandardCharsets.UTF_8);

            // 1. 先反序列化為一個簡單的、Jackson 可理解的 Map 結構
            TypeReference<Map<String, Map<String, Object>>> typeRef = new TypeReference<>() {};
            Map<String, Map<String, Object>> intermediateMap = objectMapper.readValue(jsonKey, typeRef);

            // 2. 手動將簡單 Map 轉換為 Map<String, AttributeValue>
            return intermediateMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> deserializeAttributeValue(entry.getValue())
                    ));
        } catch (Exception e) {
            log.error("反序列化 lastEvaluatedKey 失敗: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private AttributeValue deserializeAttributeValue(Map<String, Object> valueMap) {
        // 根據 DynamoDB JSON 格式，這個 Map 應該只會有一個 key (S, N, L 等)
        String type = valueMap.keySet().iterator().next();
        Object value = valueMap.get(type);

        return switch (type) {
            // --- 基本類型 ---
            case "S" -> AttributeValue.builder().s((String) value).build();
            case "N" -> AttributeValue.builder().n((String) value).build();
            case "B" -> {
                byte[] decodedBytes = Base64.getDecoder().decode((String) value);
                yield AttributeValue.builder().b(SdkBytes.fromByteArray(decodedBytes)).build();
                // 將 Base64 字串解碼回 SdkBytes 物件
            }
            case "BOOL" -> AttributeValue.builder().bool((Boolean) value).build();
            case "NULL" -> AttributeValue.builder().nul((Boolean) value).build();

            // --- 集合類型 (Set) ---
            case "SS" ->
                // Jackson 會將 JSON 陣列解析成 List
                    AttributeValue.builder().ss((List<String>) value).build();
            case "NS" -> AttributeValue.builder().ns((List<String>) value).build();
            case "BS" -> {
                List<SdkBytes> sdkBytesList = ((List<String>) value).stream()
                        .map(s -> SdkBytes.fromByteArray(Base64.getDecoder().decode(s)))
                        .collect(Collectors.toList());
                yield AttributeValue.builder().bs(sdkBytesList).build();
                // 將 List 中的每一個 Base64 字串都解碼成 SdkBytes
            }

            // --- 巢狀/遞迴類型 ---
            case "L" -> {
                List<AttributeValue> deserializedList = ((List<Map<String, Object>>) value).stream()
                        .map(this::deserializeAttributeValue)
                        .collect(Collectors.toList());
                yield AttributeValue.builder().l(deserializedList).build();
                // 將 List 中的每一個元素，遞迴地呼叫自己進行轉換
            }
            case "M" -> {
                Map<String, AttributeValue> deserializedMap = ((Map<String, Map<String, Object>>) value).entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> deserializeAttributeValue(entry.getValue())
                        ));
                yield AttributeValue.builder().m(deserializedMap).build();
                // 將 Map 中的每一個 Value，遞迴地呼叫自己進行轉換
            }
            default ->
                    throw new IllegalArgumentException("Unsupported type in lastEvaluatedKey for deserialization: " + type);
        };
    }

    private String serializeKey(Map<String, AttributeValue> key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        try {
            Map<String, Map<String, Object>> serializableKey = key.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> serializeAttributeValue(entry.getValue())
                    ));

            String jsonString = objectMapper.writeValueAsString(serializableKey);
            return Base64.getUrlEncoder().encodeToString(jsonString.getBytes());

        } catch (JsonProcessingException e) {
            log.error("序列化 DynamoDB key 失敗", e);
            throw new RuntimeException("無法序列化分頁 key", e);
        }
    }

    private Map<String, Object> serializeAttributeValue(AttributeValue val) {
        // --- 基本類型 ---
        if (val.s() != null) return Map.of("S", val.s());
        if (val.n() != null) return Map.of("N", val.n());
        if (val.b() != null) return Map.of("B", Base64.getEncoder().encodeToString(val.b().asByteArray()));
        if (val.bool() != null) return Map.of("BOOL", val.bool());
        if (val.nul() != null && val.nul()) return Map.of("NULL", true);

        // --- 集合類型 (Set) ---
        // AWS SDK v2 使用 hasSs() 這類方法來檢查集合是否存在且非空
        if (val.hasSs()) return Map.of("SS", val.ss());
        if (val.hasNs()) return Map.of("NS", val.ns());
        if (val.hasBs()) {
            // 處理二進位集合，需要將其中每個元素都進行 Base64 編碼
            List<String> base64Strings = val.bs().stream()
                    .map(sdkBytes -> Base64.getEncoder().encodeToString(sdkBytes.asByteArray()))
                    .toList();
            return Map.of("BS", base64Strings);
        }

        // --- 巢狀/遞迴類型 ---
        if (val.hasL()) {
            // 處理列表，遞迴地轉換列表中的每一個 AttributeValue
            List<Map<String, Object>> serializedList = val.l().stream()
                    .map(this::serializeAttributeValue) // 對每個元素再次呼叫自己
                    .toList();
            return Map.of("L", serializedList);
        }
        if (val.hasM()) {
            // 處理 Map，遞迴地轉換 Map 中的每一個 AttributeValue
            Map<String, Map<String, Object>> serializedMap = val.m().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> serializeAttributeValue(entry.getValue()) // 對每個 Value 再次呼叫自己
                    ));
            return Map.of("M", serializedMap);
        }

        throw new IllegalStateException("Unsupported AttributeValue type for serialization: " + val);

    }


    public Map<String, AttributeValue> getKey(OrderPo orderPo) {
        Map<String, AttributeValue> keyMap = new HashMap<>();

        if (orderPo.getOrderId() != null) {
            keyMap.put("pk", AttributeValue.builder().s(orderPo.getOrderId()).build());
        }

        keyMap.put("sk", AttributeValue.builder().s("info").build());

        if (orderPo.getServiceTypeId() != null) {
            keyMap.put("service_type_id", AttributeValue.builder().s(orderPo.getServiceTypeId()).build());
        }

        if (orderPo.getCreatedAt() != null) {
            keyMap.put("created_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(orderPo.getCreatedAt())).build());
        }

        return keyMap;
    }

}
