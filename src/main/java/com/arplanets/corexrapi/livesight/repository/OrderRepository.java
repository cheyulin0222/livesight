package com.arplanets.corexrapi.livesight.repository;

import com.arplanets.corexrapi.livesight.model.dto.req.OrderFilterRequest;
import com.arplanets.corexrapi.livesight.model.dto.res.PageResult;
import com.arplanets.corexrapi.livesight.model.po.OrderPo;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderRepository {

    OrderPo create(OrderPo order);

    Optional<OrderPo> findById(String orderId);

    OrderPo update(OrderPo order);

    PageResult<OrderPo> pageByServiceTypeId(String serviceTypeId, ZonedDateTime startDate, ZonedDateTime endDate, Integer pageSize, String lastEvaluatedKey);

    List<OrderPo> listByServiceTypeId(String serviceTypeId, OrderFilterRequest filters);

}
