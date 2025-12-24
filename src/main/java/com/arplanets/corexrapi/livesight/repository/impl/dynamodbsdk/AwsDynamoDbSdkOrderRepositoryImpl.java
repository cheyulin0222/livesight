package com.arplanets.corexrapi.livesight.repository.impl.dynamodbsdk;

import com.arplanets.corexrapi.livesight.exception.OrderApiException;
import com.arplanets.corexrapi.livesight.exception.enums.OrderErrorCode;
import com.arplanets.corexrapi.livesight.model.dto.req.DateRangeRequest;
import com.arplanets.corexrapi.livesight.model.dto.req.OrderFilterRequest;
import com.arplanets.corexrapi.livesight.model.dto.res.PageResult;
import com.arplanets.corexrapi.livesight.model.eunms.OrderStatus;
import com.arplanets.corexrapi.livesight.model.po.OrderPo;
import com.arplanets.corexrapi.livesight.repository.OrderRepository;
import com.arplanets.commons.utils.DateTimeConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
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

import java.lang.reflect.Field;
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

        if (order.getPlanId() != null) {
            item.put("plan_id", AttributeValue.builder().s(order.getPlanId()).build());
        }

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
                    .tags(item.get("tags") != null ? mapAttributeValueListToStringList(item.get("tags").l()) : null)
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

        StringBuilder updateExpression = new StringBuilder("SET ");
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();

        Optional.ofNullable(order.getOrderStatus()).ifPresent(s -> {
            updateExpression.append("order_status = :val_order_status, ");
            expressionAttributeValues.put(":val_order_status", AttributeValue.builder().s(s.name()).build());
        });

        Optional.ofNullable(order.getVerificationCode()).ifPresent(s -> {
            updateExpression.append("verification_code = :val_verification_code, ");
            expressionAttributeValues.put(":val_verification_code", AttributeValue.builder().s(s).build());
        });

        Optional.ofNullable(order.getActivatedAt()).ifPresent(s -> {
            updateExpression.append("activated_at = :val_activated_at, ");
            expressionAttributeValues.put(":val_activated_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(s)).build());
        });

        Optional.ofNullable(order.getActivatedBy()).ifPresent(s -> {
            updateExpression.append("activated_by = :val_activated_by, ");
            expressionAttributeValues.put(":val_activated_by", AttributeValue.builder().s(s).build());
        });

        Optional.ofNullable(order.getRedeemCode()).ifPresent(s -> {
            updateExpression.append("redeem_code = :val_redeem_code, ");
            expressionAttributeValues.put(":val_redeem_code", AttributeValue.builder().s(s).build());
        });

        Optional.ofNullable(order.getRedeemedAt()).ifPresent(s -> {
            updateExpression.append("redeemed_at = :val_redeemed_at, ");
            expressionAttributeValues.put(":val_redeemed_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(s)).build());
        });

        Optional.ofNullable(order.getAccessToken()).ifPresent(s -> {
            updateExpression.append("access_token = :val_access_token, ");
            expressionAttributeValues.put(":val_access_token", AttributeValue.builder().s(s).build());
        });

        Optional.ofNullable(order.getVoidedAt()).ifPresent(s -> {
            updateExpression.append("voided_at = :val_voided_at, ");
            expressionAttributeValues.put(":val_voided_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(s)).build());
        });

        Optional.ofNullable(order.getVoidedBy()).ifPresent(s -> {
            updateExpression.append("voided_by = :val_voided_by, ");
            expressionAttributeValues.put(":val_voided_by", AttributeValue.builder().s(s).build());
        });

        Optional.ofNullable(order.getReturnedAt()).ifPresent(s -> {
            updateExpression.append("returned_at = :val_returned_at, ");
            expressionAttributeValues.put(":val_returned_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(s)).build());
        });

        Optional.ofNullable(order.getReturnedBy()).ifPresent(s -> {
                updateExpression.append("returned_by = :val_returned_by, ");
                expressionAttributeValues.put(":val_returned_by", AttributeValue.builder().s(s).build());
        });

        Optional.ofNullable(order.getExpiredAt()).ifPresent(s -> {
            updateExpression.append("expired_at = :val_expired_at, ");
            expressionAttributeValues.put(":val_expired_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(s)).build());
        });

        Optional.ofNullable(order.getTags()).ifPresent(tags -> {
            List<AttributeValue> tagList = tags.stream()
                    .filter(Objects::nonNull)
                    .map(string -> AttributeValue.builder().s(string).build())
                    .toList();

            if (!tagList.isEmpty()) {
                updateExpression.append("tags = :val_tags, ");
                expressionAttributeValues.put(":val_tags", AttributeValue.builder().l(tagList).build());
            }
        });

        // 如果沒有任何欄位要更新，則直接返回，避免發送無效請求
        if (expressionAttributeValues.isEmpty()) {
            return order;
        }

        Optional.ofNullable(order.getUpdatedAt()).ifPresent(s -> {
            updateExpression.append("updated_at = :val_updated_at, ");
            expressionAttributeValues.put(":val_updated_at", AttributeValue.builder().s(DateTimeConverter.toFormattedString(s)).build());
        });

        // 移除 updateExpression 最後多出來的 ", "
        updateExpression.setLength(updateExpression.length() - 2);

        String conditionExpression = null;
        if (order.getOrderStatus() == OrderStatus.VOIDED) {
            conditionExpression = "order_status <> :expectedStatus AND product_id = :expectedProductId AND namespace = :expectedNamespace AND attribute_exists(order_id)";
            // 驗證 status
            expressionAttributeValues.put(":expectedStatus", AttributeValue.builder().s(OrderStatus.VOIDED.name()).build());
            // 驗證 ProductId
            expressionAttributeValues.put(":expectedProductId", AttributeValue.builder().s(order.getProductId()).build());
            // 驗證 namespace
            expressionAttributeValues.put(":expectedNamespace", AttributeValue.builder().s(order.getNamespace()).build());
        } else if (order.getOrderStatus() == OrderStatus.ACTIVATED) {
            conditionExpression = "order_status = :expectedStatus AND product_id = :expectedProductId AND namespace = :expectedNamespace AND expired_at > :now AND attribute_exists(order_id)";
            // 驗證 status
            expressionAttributeValues.put(":expectedStatus", AttributeValue.builder().s(OrderStatus.PENDING.name()).build());
            // 驗證 Product ID
            expressionAttributeValues.put(":expectedProductId", AttributeValue.builder().s(order.getProductId()).build());
            // 驗證 namespace
            expressionAttributeValues.put(":expectedNamespace", AttributeValue.builder().s(order.getNamespace()).build());
            // 驗證效期
            expressionAttributeValues.put(":now", AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getUpdatedAt())).build());
        } else if (order.getOrderStatus() == OrderStatus.REDEEMED) {
            conditionExpression = "order_status = :expectedStatus AND expired_at > :now AND attribute_exists(order_id) AND redeem_code = :redeemCode AND product_id = :expectedProductId";
            // 驗證 Redeem Code
            expressionAttributeValues.put(":redeemCode", AttributeValue.builder().s(order.getRedeemCode()).build());
            // 驗證 status
            expressionAttributeValues.put(":expectedStatus", AttributeValue.builder().s(OrderStatus.ACTIVATED.name()).build());
            // 驗證 Product ID
            expressionAttributeValues.put(":expectedProductId", AttributeValue.builder().s(order.getProductId()).build());
            // 驗證效期
            expressionAttributeValues.put(":now", AttributeValue.builder().s(DateTimeConverter.toFormattedString(order.getUpdatedAt())).build());
        } else if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            conditionExpression = "order_status = :expectedStatus AND product_id = :expectedProductId AND namespace = :expectedNamespace AND attribute_exists(order_id)";
            // 驗證 status
            expressionAttributeValues.put(":expectedStatus", AttributeValue.builder().s(OrderStatus.REDEEMED.name()).build());
            // 驗證 ProductId
            expressionAttributeValues.put(":expectedProductId", AttributeValue.builder().s(order.getProductId()).build());
            // 驗證 namespace
            expressionAttributeValues.put(":expectedNamespace", AttributeValue.builder().s(order.getNamespace()).build());
        }


        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(PK_ATTRIBUTE_NAME, AttributeValue.builder().s(order.getOrderId()).build(),
                        SK_ATTRIBUTE_NAME, AttributeValue.builder().s(SK_VALUE).build()))
                .updateExpression(updateExpression.toString())
                .conditionExpression(conditionExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        try {
            UpdateItemResponse response = dynamoDbClient.updateItem(updateItemRequest);
            order = mapToOrderPo(response.attributes());
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
        String keyConditionExpression = buildKeyCondition(startDate, endDate);
        Map<String, AttributeValue> expressionAttributeValues = initAttributeValue(serviceTypeId, startDate, endDate);

        QueryRequest.Builder requestBuilder = QueryRequest.builder()
                .tableName(tableName)
                .indexName("service_type_id-created_at-index")
                .keyConditionExpression(keyConditionExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .scanIndexForward(false)
                // 取多一筆數量，為了判斷有沒有下一筆
                .limit(pageSize + 1);


        Map<String, AttributeValue> lastEvaluatedKeyMap = deserializeKey(lastEvaluatedKey);

        if (lastEvaluatedKeyMap != null && !lastEvaluatedKeyMap.isEmpty()) {
            requestBuilder.exclusiveStartKey(lastEvaluatedKeyMap);
        }

        QueryResponse response = sendQuery(requestBuilder.build());

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

    @Override
    public List<OrderPo> listByServiceTypeId(String serviceTypeId, OrderFilterRequest filters) {
        List<OrderPo> allOrders = new ArrayList<>();
        boolean hasMorePage = true;

        // 取得 start_date 和 end_date
        ZonedDateTime startDate = filters.getCreatedAt() != null ? filters.getCreatedAt().getStartDate() : null;
        ZonedDateTime endDate = filters.getCreatedAt() != null ? filters.getCreatedAt().getEndDate() : null;

        // 使用 start_date 和 end_date 產生基本的高效查詢條件
        String keyConditionExpression = buildKeyCondition(startDate, endDate);
        Map<String, AttributeValue> expressionAttributeValues = initAttributeValue(serviceTypeId, startDate, endDate);

        // 產生額外查詢條件
        Map<String, Object> filterParts = buildFilterExpression(filters);

        // 取得額外條件
        String filterExpression = (String) filterParts.get("expression");

        // 取得所有參數名稱
        @SuppressWarnings("unchecked")
        Map<String, String> expressionAttributeNames = (Map<String, String>) filterParts.get("names");

        // 取得所有的參數值
        @SuppressWarnings("unchecked")
        Map<String, AttributeValue> filterExpressionValues = (Map<String, AttributeValue>) filterParts.get("values");
        expressionAttributeValues.putAll(filterExpressionValues);

        Map<String, AttributeValue> exclusiveStartKey = null;

        while (hasMorePage) {
            QueryRequest.Builder requestBuilder = QueryRequest.builder()
                    .tableName(tableName)
                    .indexName("service_type_id-created_at-index")
                    .keyConditionExpression(keyConditionExpression)
                    .expressionAttributeValues(expressionAttributeValues)
                    .scanIndexForward(false)
                    .limit(1000);

            // 設定起始索引
            if (exclusiveStartKey != null && !exclusiveStartKey.isEmpty()) {
                requestBuilder.exclusiveStartKey(exclusiveStartKey);
            }

            // 設定所有額外的參數名稱
            if (!expressionAttributeNames.isEmpty()) {
                requestBuilder.expressionAttributeNames(expressionAttributeNames);
            }

            // 設定額外查詢條件
            if (!filterExpression.isEmpty()) {
                requestBuilder.filterExpression(filterExpression);
            }

            // 送出查詢
            QueryResponse response = sendQuery(requestBuilder.build());

            // 取得結果
            List<OrderPo> currentOrders = response.items().stream()
                    .map(this::mapToOrderPo)
                    .toList();

            // 加入最終結果
            allOrders.addAll(currentOrders);

            // 設定是否要繼續查詢
            if (response.lastEvaluatedKey() != null && !response.lastEvaluatedKey().isEmpty()) {
                exclusiveStartKey = response.lastEvaluatedKey();
            } else {
                hasMorePage = false;
            }
        }


        return allOrders;
    }

    private String buildKeyCondition(ZonedDateTime startDate, ZonedDateTime endDate) {
        String keyConditionExpression = "service_type_id = :service_type_id";

        if (startDate != null && endDate != null && endDate.isAfter(startDate)) {
            keyConditionExpression += " AND created_at BETWEEN :start_date AND :end_date";
        } else if (startDate != null) {
            keyConditionExpression += " AND created_at >= :start_date";
        } else if (endDate != null) {
            keyConditionExpression += " AND created_at <= :end_date";
        }

        return keyConditionExpression;
    }

    private QueryResponse sendQuery(QueryRequest request) {
        try {
            return dynamoDbClient.query(request);
        } catch (DynamoDbException e) {
            log.error("查詢 DynamoDB 失敗: {}", e.getMessage());
            throw new RuntimeException("查詢訂單列表失敗。", e);
        }
    }

    private Map<String, Object> buildFilterExpression(OrderFilterRequest filters) {
        if (filters == null) {
            return Map.of("expression", "", "names", Map.of(), "values", Map.of());
        }

        ExpressionContext context = new ExpressionContext();

        Class<? extends OrderFilterRequest> clazz = filters.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            try {
                field.setAccessible(true);

                Object fieldValue = field.get(filters);

                String fieldName = getJsonFieldName(field);

                if ("created_at".equals(fieldName)) continue;

                if (fieldValue instanceof DateRangeRequest range) {
                    handleDateRange(context, fieldName, range);
                } else if ("tags".equals(fieldName) && fieldValue instanceof List) {
                    handleTags(context, fieldName, (List<?>) fieldValue);
                } else if (fieldValue != null) {
                    handleSingleValue(context, fieldName, fieldValue);
                }
            } catch (IllegalAccessException e) {
                log.error("無法訪問屬性: {}", e.getMessage());
            } finally {
                field.setAccessible(false);
            }
        }

        return Map.of(
                "expression", context.expressionBuilder.toString(),
                "names", context.expressionAttributeNames,
                "values", context.expressionAttributeValues
        );

    }

    private String getJsonFieldName(Field field) {
        if (field.isAnnotationPresent(JsonProperty.class)) {
            String jsonName = field.getAnnotation(JsonProperty.class).value();
            if (!jsonName.isEmpty()) {
                return jsonName;
            }
        }
        return field.getName();
    }

    private Map<String, AttributeValue> initAttributeValue(String serviceTypeId, ZonedDateTime startDate, ZonedDateTime endDate) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":service_type_id", AttributeValue.builder().s(serviceTypeId).build());

        if (startDate != null && endDate != null && endDate.isAfter(startDate)) {
            expressionAttributeValues.put(":start_date", AttributeValue.builder().s(DateTimeConverter.toFormattedString(startDate)).build());
            expressionAttributeValues.put(":end_date", AttributeValue.builder().s(DateTimeConverter.toFormattedString(endDate)).build());
        } else if (startDate != null) {
            expressionAttributeValues.put(":start_date", AttributeValue.builder().s(DateTimeConverter.toFormattedString(startDate)).build());
        } else if (endDate != null) {
            expressionAttributeValues.put(":end_date", AttributeValue.builder().s(DateTimeConverter.toFormattedString(endDate)).build());
        }

        return expressionAttributeValues;
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
                .tags(item.get("tags") != null ? mapAttributeValueListToStringList(item.get("tags").l()) : null)
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

    private List<String> mapAttributeValueListToStringList(List<AttributeValue> avList) {
        if (avList == null) {
            return null;
        }
        return avList.stream()
                .map(AttributeValue::s)
                .collect(Collectors.toList());
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

    private void handleDateRange(ExpressionContext context, String fieldName, DateRangeRequest range) {
        ZonedDateTime start = range.getStartDate();
        ZonedDateTime end = range.getEndDate();

        if (start == null && end == null) return;

        String nameAlias = "#" + fieldName + context.index;
        String startAlias = ":" + fieldName + "Start" + context.index;
        String endAlias = ":" + fieldName + "End" + context.index;

        String expression;

        if (start != null && end != null) {
            expression = nameAlias + " BETWEEN " + startAlias + " AND " + endAlias;
            context.expressionAttributeValues.put(startAlias, AttributeValue.builder().s(DateTimeConverter.toFormattedString(start)).build());
            context.expressionAttributeValues.put(endAlias, AttributeValue.builder().s(DateTimeConverter.toFormattedString(end)).build());
        } else if (start != null) {
            expression = nameAlias + " >= " + startAlias;
            context.expressionAttributeValues.put(startAlias, AttributeValue.builder().s(DateTimeConverter.toFormattedString(start)).build());
        } else { // end != null
            expression = nameAlias + " <= " + endAlias;
            context.expressionAttributeValues.put(endAlias, AttributeValue.builder().s(DateTimeConverter.toFormattedString(end)).build());
        }

        if (!context.expressionBuilder.isEmpty()) {
            context.expressionBuilder.append(" AND ");
        }

        context.expressionBuilder.append(expression);
        context.expressionAttributeNames.put(nameAlias, fieldName);
        context.index++;

    }

    private void handleTags(ExpressionContext context, String fieldName, List<?> rawList) {
        if (rawList.isEmpty() || !(rawList.get(0) instanceof String)) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> tagList = (List<String>) rawList;

        String nameAlias = "#" + fieldName + context.index;
        context.expressionAttributeNames.put(nameAlias, fieldName);

        // 2. 構建 tags 的子表達式
        StringBuilder tagsSubExpression = new StringBuilder();
        int innerIndex = 0;

        for (String tag : tagList) {
            String tagValueAlias = ":" + fieldName + "Val" + context.index + "_" + innerIndex;

            if (!tagsSubExpression.isEmpty()) {
                tagsSubExpression.append(" AND ");
            }

            // 語法：contains(#tags5, :tagsVal5_0)
            tagsSubExpression.append("contains(").append(nameAlias).append(", ").append(tagValueAlias).append(")");

            context.expressionAttributeValues.put(tagValueAlias, AttributeValue.builder().s(tag).build());
            innerIndex++;
        }

        // 3. 加入主表達式
        if (!context.expressionBuilder.isEmpty()) {
            context.expressionBuilder.append(" AND ");
        }
        context.expressionBuilder.append("(").append(tagsSubExpression).append(")");

        context.index++;

    }

    private void handleSingleValue(ExpressionContext context, String fieldName, Object fieldValue) {
        if (fieldValue instanceof String s && s.isBlank()) {
            return;
        }

        String nameAlias = "#" + fieldName + context.index;
        String valueAlias = ":" + fieldName + "Val" + context.index;
        AttributeValue value = null;

        if (fieldValue instanceof String s) {
            value = AttributeValue.builder().s(s).build();
        } else if (fieldValue instanceof OrderStatus status) {
            value = AttributeValue.builder().s(status.name()).build();
        } else if (fieldValue instanceof Number) {
            value = AttributeValue.builder().n(fieldValue.toString()).build();
        } else if (fieldValue instanceof Boolean) {
            value = AttributeValue.builder().bool((Boolean) fieldValue).build();
        }

        if (value != null) {
            String expression = nameAlias + " = " + valueAlias;
            context.appendCondition(nameAlias, fieldName, expression, value, valueAlias);
        }

    }

    private static class ExpressionContext {
        StringBuilder expressionBuilder = new StringBuilder();
        Map<String, String> expressionAttributeNames = new HashMap<>();
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        int index = 0;

        // 封裝 AND 連接和 index 遞增邏輯
        private void appendCondition(String nameAlias, String fieldName, String expression, AttributeValue value, String valueAlias) {
            if (!expressionBuilder.isEmpty()) {
                expressionBuilder.append(" AND ");
            }
            expressionBuilder.append(expression);
            expressionAttributeNames.put(nameAlias, fieldName);
            expressionAttributeValues.put(valueAlias, value);
            index++;
        }
    }

}
