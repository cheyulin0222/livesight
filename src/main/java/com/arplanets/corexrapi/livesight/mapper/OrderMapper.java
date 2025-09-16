package com.arplanets.corexrapi.livesight.mapper;

import com.arplanets.corexrapi.livesight.model.OrderContext;
import com.arplanets.corexrapi.livesight.model.bo.OrderIotPayload;
import com.arplanets.corexrapi.livesight.model.dto.OrderDto;
import com.arplanets.corexrapi.livesight.model.dto.res.*;
import com.arplanets.corexrapi.livesight.model.po.OrderPo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderContext orderPoToOrderContext(OrderPo orderPo);
    OrderDto orderPoToOrderDto(OrderPo orderPo);
    OrderCreateResponse orderDtoToOrderCreateResponse(OrderDto orderDto);
    OrderStatusResponse orderDtoToOrderStatusResponse(OrderDto orderDto);
    OrderRedeemResponse orderDtoToOrderRedeemResponse(OrderDto orderDto);
    OrderInfoResponse orderDtoToOrderInfoResponse(OrderDto orderDto);
    OrderActivateResponse orderDtoToOrderActivateResponse(OrderDto orderDto);
    OrderVoidResponse orderDtoToOrderVoidResponse(OrderDto orderDto);
    OrderReturnResponse orderDtoToOrderReturnResponse(OrderDto orderDto);
    OrderListResponse orderDtoToOrderListResponse(OrderDto orderDto);
    OrderIotPayload orderPoToOrderIotPayload(OrderPo orderPo);
}
