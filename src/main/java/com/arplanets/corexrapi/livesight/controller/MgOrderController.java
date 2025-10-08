package com.arplanets.corexrapi.livesight.controller;

import com.arplanets.corexrapi.livesight.mapper.OrderMapper;
import com.arplanets.corexrapi.livesight.model.dto.OrderDto;
import com.arplanets.corexrapi.livesight.model.dto.req.*;
import com.arplanets.corexrapi.livesight.model.dto.res.*;
import com.arplanets.corexrapi.livesight.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mg/api/order")
@RequiredArgsConstructor
@Tag(name="(後台) 訂單 API", description = "(後台) 訂單 API")
public class MgOrderController {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @PostMapping(value = "/info", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "查詢訂單資訊", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("@permissionChecker.checkOrgMemberPermission(#request.orgId, #authentication)")
    public ResponseEntity<OrderInfoResponse> getOrderInfo(@RequestBody @Valid OrderInfoRequest request, Authentication authentication) {
        OrderDto result = orderService.getOrder(
                request.getProductId(),
                request.getOrgId(),
                request.getNamespace(),
                request.getOrderId());

        return ResponseEntity.ok(orderMapper.orderDtoToOrderInfoResponse(result));
    }

    @PostMapping(value = "/list", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "查詢訂單列表", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("@permissionChecker.checkOrgMemberPermission(#request.orgId, #authentication)")
    public ResponseEntity<PageResult<OrderListResponse>> listOrders(@RequestBody @Valid OrderListRequest request) {

        PageResult<OrderDto> result = orderService.listOrder(
                request.getProductId(),
                request.getOrgId(),
                request.getNamespace(),
                request.getDateRange().getStartDate(),
                request.getDateRange().getEndDate(),
                request.getPage());

        return ResponseEntity.ok(result.mapItems(orderMapper::orderDtoToOrderListResponse));

    }

    @PostMapping(value = "/activate", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "開通訂單", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("@permissionChecker.checkOrgMemberPermission(#orderRequest.orgId, #authentication)")
    public ResponseEntity<OrderActivateResponse> activateOrder(@RequestBody @Valid OrderActivateRequest orderRequest, Authentication authentication, HttpServletRequest request) {

        String username = null;

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            username = jwt.getClaimAsString("username");
        }

        OrderDto result = orderService.activateOrder(
                request,
                orderRequest.getProductId(),
                orderRequest.getOrgId(),
                orderRequest.getNamespace(),
                orderRequest.getOrderId(),
                username);

        return ResponseEntity.ok(orderMapper.orderDtoToOrderActivateResponse(result));
    }

    @PostMapping(value = "/void", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "作廢訂單", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("@permissionChecker.checkOrgMemberPermission(#orderRequest.orgId, #authentication)")
    public ResponseEntity<OrderVoidResponse> voidOrder(@RequestBody @Valid OrderVoidRequest orderRequest, Authentication authentication, HttpServletRequest request) {
        String username = null;

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            username = jwt.getClaimAsString("username");
        }

        OrderDto result = orderService.voidOrder(
                request,
                orderRequest.getProductId(),
                orderRequest.getOrgId(),
                orderRequest.getNamespace(),
                orderRequest.getOrderId(),
                username);

        return ResponseEntity.ok(orderMapper.orderDtoToOrderVoidResponse(result));

    }

    @PostMapping(value = "/return", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "歸還訂單", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("@permissionChecker.checkOrgMemberPermission(#orderRequest.orgId, #authentication)")
    public ResponseEntity<OrderReturnResponse> returnOrder(@RequestBody @Valid OrderReturnRequest orderRequest, Authentication authentication, HttpServletRequest request) {
        String username = null;

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            username = jwt.getClaimAsString("username");
        }

        OrderDto result = orderService.returnOrder(
                request,
                orderRequest.getProductId(),
                orderRequest.getOrgId(),
                orderRequest.getNamespace(),
                orderRequest.getOrderId(),
                username
        );

        return ResponseEntity.ok(orderMapper.orderDtoToOrderReturnResponse(result));
    }
}
