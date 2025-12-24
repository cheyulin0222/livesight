package com.arplanets.corexrapi.livesight.repository;

import com.arplanets.corexrapi.livesight.model.po.PlanPo;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {

    PlanPo create(PlanPo plan);
    List<PlanPo> createWithTransaction(List<PlanPo> plans);
    PlanPo update(PlanPo plan);
    List<PlanPo> updateWithTransaction(List<PlanPo> plans);
    List<PlanPo> listByLiveSightId(String liveSightId);
    Optional<PlanPo> findById(String planeId);
}
