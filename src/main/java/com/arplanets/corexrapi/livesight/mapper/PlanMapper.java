package com.arplanets.corexrapi.livesight.mapper;

import com.arplanets.corexrapi.livesight.model.dto.PlanDto;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanCreateResponse;
import com.arplanets.corexrapi.livesight.model.dto.res.PlanUpdateResponse;
import com.arplanets.corexrapi.livesight.model.po.PlanPo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlanMapper {

    PlanDto planePoToPlaneDto(PlanPo plane);
    PlanCreateResponse planPoToPlanCreateResponse(PlanPo plane);
    PlanUpdateResponse planPoToPlanUpdateResponse(PlanPo plane);
}
