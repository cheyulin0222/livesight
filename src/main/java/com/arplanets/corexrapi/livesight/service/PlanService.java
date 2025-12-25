package com.arplanets.corexrapi.livesight.service;

import com.arplanets.corexrapi.livesight.model.dto.PlanDto;
import com.arplanets.corexrapi.livesight.model.dto.req.PlanCreateRequest;
import com.arplanets.corexrapi.livesight.model.dto.req.PlanUpdateRequest;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanBatchCreateResponse;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanBatchUpdateResponse;

import java.util.List;
import java.util.Map;

public interface PlanService {

    PlanBatchCreateResponse batchCreatePlan(List<PlanCreateRequest> planes, String liveSightId, String user);
    PlanBatchUpdateResponse batchUpdatePlan(List<PlanUpdateRequest> planes, String liveSightId, String user);
    Map<String, PlanDto> findByLiveSightId(String liveSightId);
}
