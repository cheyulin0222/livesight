package com.arplanets.LiveSight.authorization.service;

import com.arplanets.LiveSight.authorization.model.dto.OrderDto;
import com.arplanets.LiveSight.authorization.model.dto.req.PageRequest;
import com.arplanets.LiveSight.authorization.model.dto.res.PageResult;
import jakarta.servlet.http.HttpServletRequest;

import java.time.ZonedDateTime;

public interface OrderService {

    OrderDto createOrder(HttpServletRequest request, String productId, String namespace, String authType, String authTypeId, String salt);
    OrderDto getOrderStatus(String productId, String orderId, String salt);
    OrderDto getOrder(String productId, String orgId, String namespace, String orderId);
    OrderDto redeemOrder(HttpServletRequest request, String productId, String orderId, String redeemCode);
    OrderDto activateOrder(HttpServletRequest request, String productId, String orgId, String namespace, String orderId, String staffId);
    OrderDto voidOrder(HttpServletRequest request, String productId, String orgId, String namespace, String orderId, String staffId);
    OrderDto returnOrder(HttpServletRequest request, String productId, String orgId, String namespace, String orderId, String staffId);
    PageResult<OrderDto> listOrder(String productId, String orgId, String namespace, ZonedDateTime startDate, ZonedDateTime endDate, PageRequest page);
    void verifyToken(String accessToken);


}
