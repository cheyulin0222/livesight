package com.arplanets.corexrapi.livesight.controller;

import com.arplanets.corexrapi.livesight.mapper.OrderMapper;
import com.arplanets.corexrapi.livesight.model.dto.req.OrderCreateRequest;
import com.arplanets.corexrapi.livesight.model.dto.req.OrderFetchStatusRequest;
import com.arplanets.corexrapi.livesight.model.dto.req.OrderRedeemRequest;
import com.arplanets.corexrapi.livesight.model.dto.res.OrderCreateResponse;
import com.arplanets.corexrapi.livesight.model.dto.OrderDto;
import com.arplanets.corexrapi.livesight.model.dto.res.OrderRedeemResponse;
import com.arplanets.corexrapi.livesight.model.dto.res.OrderStatusResponse;
import com.arplanets.corexrapi.livesight.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/live-sight/api/order")
@RequiredArgsConstructor
@Tag(name = "訂單", description = "訂單 API")
public class ApiOrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping(value = "/create", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "建立訂單")
    public ResponseEntity<OrderCreateResponse> createOrder(@RequestBody @Valid OrderCreateRequest orderRequest, HttpServletRequest request) {
        OrderDto result = orderService.createOrder(
                request,
                orderRequest.getProductId(),
                orderRequest.getNamespace(),
                orderRequest.getAuthType(),
                orderRequest.getAuthTypeId(),
                orderRequest.getSalt());

        return ResponseEntity.ok(orderMapper.orderDtoToOrderCreateResponse(result));
    }

    @PostMapping(value = "/fetch_status", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "查詢訂單狀態")
    public ResponseEntity<OrderStatusResponse> fetchStatus(@RequestBody @Valid OrderFetchStatusRequest request) {
        OrderDto resul = orderService.getOrderStatus(
                request.getProductId(),
                request.getOrderId(),
                request.getSalt());

        return ResponseEntity.ok(orderMapper.orderDtoToOrderStatusResponse(resul));

    }

    @PostMapping(value = "/redeem", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "領取訂單")
    public ResponseEntity<OrderRedeemResponse> redeem(@RequestBody @Valid OrderRedeemRequest orderRequest, HttpServletRequest request) {
        OrderDto result = orderService.redeemOrder(
                request,
                orderRequest.getProductId(),
                orderRequest.getOrderId(),
                orderRequest.getRedeemCode());

        return ResponseEntity.ok(orderMapper.orderDtoToOrderRedeemResponse(result));
    }
}
