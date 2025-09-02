package com.arplanets.LiveSight.authorization.repository;

import com.arplanets.LiveSight.authorization.model.dto.res.PageResult;
import com.arplanets.LiveSight.authorization.model.po.OrderPo;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface OrderRepository {

    OrderPo create(OrderPo order);

    Optional<OrderPo> findById(String orderId);

    OrderPo update(OrderPo order);

    PageResult<OrderPo> pageByServiceTypeId(String serviceTypeId, ZonedDateTime startDate, ZonedDateTime endDate, Integer pageSize, String lastEvaluatedKey);
}
