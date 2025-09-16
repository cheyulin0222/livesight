package com.arplanets.corexrapi.livesight.repository;

import com.arplanets.corexrapi.livesight.model.dto.res.PageResult;
import com.arplanets.corexrapi.livesight.model.po.OrderPo;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface OrderRepository {

    OrderPo create(OrderPo order);

    Optional<OrderPo> findById(String orderId);

    OrderPo update(OrderPo order);

    PageResult<OrderPo> pageByServiceTypeId(String serviceTypeId, ZonedDateTime startDate, ZonedDateTime endDate, Integer pageSize, String lastEvaluatedKey);
}
