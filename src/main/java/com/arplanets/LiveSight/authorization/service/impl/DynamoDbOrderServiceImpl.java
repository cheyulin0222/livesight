package com.arplanets.LiveSight.authorization.service.impl;

import com.arplanets.LiveSight.authorization.aspect.OrderIdLock;
import com.arplanets.LiveSight.authorization.exception.OrderApiException;
import com.arplanets.LiveSight.authorization.exception.enums.OrderErrorCode;
import com.arplanets.LiveSight.authorization.log.ErrorContext;
import com.arplanets.LiveSight.authorization.log.Logger;
import com.arplanets.LiveSight.authorization.model.ClientInfo;
import com.arplanets.LiveSight.authorization.mapper.OrderMapper;
import com.arplanets.LiveSight.authorization.model.ResponseContext;
import com.arplanets.LiveSight.authorization.model.bo.OrderIotPayload;
import com.arplanets.LiveSight.authorization.model.dto.LiveSightDto;
import com.arplanets.LiveSight.authorization.model.dto.OrderDto;
import com.arplanets.LiveSight.authorization.model.dto.req.PageRequest;
import com.arplanets.LiveSight.authorization.model.dto.res.PageResult;
import com.arplanets.LiveSight.authorization.model.eunms.OrderStatus;
import com.arplanets.LiveSight.authorization.model.po.OrderPo;
import com.arplanets.LiveSight.authorization.repository.OrderRepository;
import com.arplanets.LiveSight.authorization.security.jwt.OrderJwtManager;
import com.arplanets.LiveSight.authorization.service.LiveSightService;
import com.arplanets.LiveSight.authorization.service.OrderService;
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
import software.amazon.awssdk.services.iotdataplane.IotDataPlaneClient;
import software.amazon.awssdk.services.iotdataplane.model.PublishRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    @Value("${aws.iot.topic.prefix}")
    private String iotTopic;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Taipei");
    public static final String LIVE_SIGHT_NAME = "livesight";
    public static final String ORDER_PREFIX = "order";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final LiveSightService liveSightService;
    private final OrderJwtManager orderJwtManager;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final IotDataPlaneClient iotDataPlaneClient;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, Object> orderLocks = new ConcurrentHashMap<>();


    @Override
    public OrderDto createOrder(HttpServletRequest request, String productId, String namespace, String authType, String authTypeId, String salt) {
        // 取得 Live Sight ID
        String liveSightId = extractUuid(namespace);

        // 驗證 Live Sight ID
        validateLiveSight(liveSightId);

        // 產生 Order ID
        String orderId = ORDER_PREFIX + "_" + UUID.randomUUID();

        // 以 Order ID 、 Salt 產生 Verification Code
        String verificationCode = hashWithSHA256(orderId, salt);

        // 取得 Client 資訊
        ClientInfo clientInfo = ClientInfoUtil.getClientInfo(request);

        // 新增訂單資料
        OrderPo result = orderRepository.create(buildCreatedOrder(orderId, namespace, productId, liveSightId, authType, authTypeId, clientInfo, verificationCode));

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
        // 取得 Live Sight ID
        String liveSightId = extractUuid(namespace);

        // 驗證 Org ID
        validateOrg(orgId, liveSightId);

        // 取得訂單資訊，若無拋出錯誤
        OrderPo result = findOrThrowByOrderId(orderId);

        // 驗證 Product ID
        validateProductId(productId, result.getProductId());

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);
    }

    @Override
    public OrderDto activateOrder(HttpServletRequest request, String productId, String orgId, String namespace, String orderId, String staffId) {

        // 取得 Live Sight ID
        String liveSightId = extractUuid(namespace);

        // 驗證 Live Sight ID
        validateLiveSight(liveSightId);

        // 驗證 Org ID
        validateOrg(orgId, liveSightId);

        // 取得訂單資訊，若無拋出錯誤
        OrderPo storedOrder = findOrThrowByOrderId(orderId);

        // 驗證 Product ID
        validateProductId(productId, storedOrder.getProductId());

        OrderStatus orderStatus = storedOrder.getOrderStatus();
        if (orderStatus == null) {
            throw new OrderApiException(OrderErrorCode._007);
        }
        if (orderStatus != OrderStatus.PENDING) {
            throw new OrderApiException(OrderErrorCode._008);
        }

        // 驗證效期
        validateExpireAt(storedOrder.getExpiredAt());

        // 產生 Redeem Code
        String redeemCode = genRedeemCode();

        // 修改訂單資料
        OrderPo result = orderRepository.update(buildActivatedOrder(storedOrder, staffId, redeemCode));

        // 將訂單資訊傳到 Iot
        sendIotRequest(result);

        // 將訂單資料暫存以做 Audit Log
        setResponseContext(request, result);

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);

    }


    @Override
    public OrderDto redeemOrder(HttpServletRequest request, String productId, String orderId, String redeemCode) {
        // 取得訂單資訊，若無拋出錯誤
        OrderPo storedOrder = findOrThrowByOrderId(orderId);

        // 驗證 Product ID
        validateProductId(productId, storedOrder.getProductId());

        // 驗證 Redeem Code
        validateRedeemCode(redeemCode, storedOrder.getRedeemCode());

        OrderStatus orderStatus = storedOrder.getOrderStatus();
        if (orderStatus == null) {
            throw new OrderApiException(OrderErrorCode._007);
        }
        if (orderStatus != OrderStatus.ACTIVATED) {
            throw new OrderApiException(OrderErrorCode._012);
        }

        // 驗證效期
        validateExpireAt(storedOrder.getExpiredAt());

        // 產生當下時間
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);

        // 產生 Access Token
        String accessToken = orderJwtManager.genAccessToken(orderId, productId, now);

        // 修改訂單資料
        OrderPo result = orderRepository.update(buildRedeemedOrder(storedOrder, accessToken, now));

        // 將訂單資料暫存以做 Audit Log
        setResponseContext(request, result);

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);

    }

    public OrderDto voidOrder(HttpServletRequest request, String productId, String orgId, String namespace, String orderId, String staffId) {
        // 取得 Live Sight ID
        String liveSightId = extractUuid(namespace);

        // 驗證 Live Sight ID
        validateLiveSight(liveSightId);

        // 驗證 Org ID
        validateOrg(orgId, liveSightId);

        // 取得訂單資訊，若無拋出錯誤
        OrderPo storedOrder = findOrThrowByOrderId(orderId);

        // 驗證 Product ID
        validateProductId(productId, storedOrder.getProductId());

        OrderStatus orderStatus = storedOrder.getOrderStatus();
        if (orderStatus == null) {
            throw new OrderApiException(OrderErrorCode._007);
        }
        if (orderStatus == OrderStatus.VOIDED) {
            throw new OrderApiException(OrderErrorCode._013);
        }

        // 修改訂單資料
        OrderPo result = orderRepository.update(buildVoidedOrder(storedOrder, staffId));

        // 將訂單資訊傳到 Iot
        sendIotRequest(result);

        // 將訂單資料暫存以做 Audit Log
        setResponseContext(request, result);

        // 回傳訂單資訊
        return orderMapper.orderPoToOrderDto(result);

    }

    @Override
    public PageResult<OrderDto> listOrder(String productId, String orgId, String namespace, ZonedDateTime startDate, ZonedDateTime endDate, PageRequest page) {
        // 取得 Live Sight ID
        String liveSightId = extractUuid(namespace);

        // 驗證 Live Sight ID
        validateLiveSight(liveSightId);

        // 驗證 Org ID
        validateOrg(orgId, liveSightId);

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
    public OrderDto returnOrder(HttpServletRequest request, String productId, String orgId, String namespace, String orderId, String staffId) {
        // 取得 Live Sight ID
        String liveSightId = extractUuid(namespace);

        // 驗證 Live Sight ID
        validateLiveSight(liveSightId);

        // 驗證 Org ID
        validateOrg(orgId, liveSightId);

        // 取得訂單資訊，若無拋出錯誤
        OrderPo storedOrder = findOrThrowByOrderId(orderId);

        // 驗證 Product ID
        validateProductId(productId, storedOrder.getProductId());

        OrderStatus orderStatus = storedOrder.getOrderStatus();
        if (orderStatus == null) {
            throw new OrderApiException(OrderErrorCode._007);
        }
        if (orderStatus != OrderStatus.REDEEMED) {
            throw new OrderApiException(OrderErrorCode._014);
        }

        // 修改訂單資料
        OrderPo result = orderRepository.update(buildReturnedOrder(storedOrder, staffId));

        // 將訂單資訊傳到 Iot
        sendIotRequest(result);

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

    private void validateLiveSight(String liveSightId) {
        if (liveSightId == null || liveSightId.isBlank()) {
            throw new OrderApiException(OrderErrorCode._001);
        }

        boolean idExist = liveSightService.isLiveSightExist(liveSightId);

        if (!idExist) {
            throw new OrderApiException(OrderErrorCode._002);
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

    private void validateExpireAt(ZonedDateTime expiredAt) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        if (expiredAt == null) {
            throw new OrderApiException(OrderErrorCode._009);
        }

        if (expiredAt.isBefore(now)) {
            throw new OrderApiException(OrderErrorCode._010);
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
            String liveSightId,
            String authType,
            String authTypeId,
            ClientInfo clientInfo,
            String verificationCode
    ) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        ZonedDateTime expiredAt = now.plusMinutes(expirationMinutes);

        return OrderPo.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.PENDING)
                .namespace(namespace)
                .productId(productId)
                .serviceType(LIVE_SIGHT_NAME)
                .serviceTypeId(liveSightId)
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

    private OrderPo buildActivatedOrder(OrderPo storedOrder, String staffId, String redeemCode) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        ZonedDateTime expiredAt = now.plusMinutes(redeemCodeExpirationMinutes);

        storedOrder.setOrderStatus(OrderStatus.ACTIVATED);
        storedOrder.setActivatedAt(now);
        storedOrder.setActivatedBy(staffId);
        storedOrder.setRedeemCode(redeemCode);
        storedOrder.setExpiredAt(expiredAt);
        storedOrder.setUpdatedAt(now);

        return storedOrder;
    }

    private OrderPo buildRedeemedOrder(OrderPo storedOrder, String accessToken, ZonedDateTime now) {
        storedOrder.setOrderStatus(OrderStatus.REDEEMED);
        storedOrder.setRedeemedAt(now);
        storedOrder.setAccessToken(accessToken);
        storedOrder.setExpiredAt(now.plusMinutes(jwtExpirationMinutes));
        storedOrder.setUpdatedAt(now);

        return storedOrder;

    }

    private OrderPo buildVoidedOrder(OrderPo storedOrder, String staffId) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);

        storedOrder.setOrderStatus(OrderStatus.VOIDED);
        storedOrder.setVoidedAt(now);
        storedOrder.setVoidedBy(staffId);
        storedOrder.setUpdatedAt(now);

        return storedOrder;
    }

    private OrderPo buildReturnedOrder(OrderPo storedOrder, String staffId) {
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);

        storedOrder.setOrderStatus(OrderStatus.COMPLETED);
        storedOrder.setReturnedAt(now);
        storedOrder.setReturnedBy(staffId);
        storedOrder.setUpdatedAt(now);

        return storedOrder;
    }

    private void validateSalt(String orderId, String salt, String storedVerificationCode) {
        String currentVerificationCode = hashWithSHA256(orderId, salt);
        if (!currentVerificationCode.equals(storedVerificationCode)) {
            throw new OrderApiException(OrderErrorCode._005);
        }
    }

    private void validateRedeemCode(String inputCode, String storedCode) {
        if (!inputCode.equals(storedCode)) {
            throw new OrderApiException(OrderErrorCode._011);
        }
    }

    private void sendIotRequest(OrderPo orderPo) {
        String topic = iotTopic + getTopicAction(orderPo) + orderPo.getOrderId();

        PublishRequest publishRequest = PublishRequest.builder()
                .topic(topic)
                .qos(1)
                .payload(buildPayload(orderPo))
                .build();

        iotDataPlaneClient.publish(publishRequest);
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
}
