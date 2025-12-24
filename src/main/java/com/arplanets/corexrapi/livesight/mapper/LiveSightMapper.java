package com.arplanets.corexrapi.livesight.mapper;

import com.arplanets.corexrapi.livesight.model.dto.LiveSightDto;
import com.arplanets.corexrapi.livesight.model.po.LiveSightPo;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface LiveSightMapper {

    LiveSightDto liveSightPoToLiveSightDto(LiveSightPo liveSightPo);

}
