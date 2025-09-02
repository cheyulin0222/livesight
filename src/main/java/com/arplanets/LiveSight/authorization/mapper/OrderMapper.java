package com.arplanets.LiveSight.authorization.mapper;

import com.arplanets.LiveSight.authorization.model.OrderContext;
import com.arplanets.LiveSight.authorization.model.bo.OrderIotPayload;
import com.arplanets.LiveSight.authorization.model.dto.OrderDto;
import com.arplanets.LiveSight.authorization.model.dto.res.*;
import com.arplanets.LiveSight.authorization.model.po.OrderPo;
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
