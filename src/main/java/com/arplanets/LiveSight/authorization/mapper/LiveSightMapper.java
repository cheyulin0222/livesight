package com.arplanets.LiveSight.authorization.mapper;

import com.arplanets.LiveSight.authorization.model.dto.LiveSightDto;
import com.arplanets.LiveSight.authorization.model.po.LiveSightPo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LiveSightMapper {

    LiveSightDto liveSightPoToLiveSightDto(LiveSightPo liveSightPo);
}
