package com.arplanets.corexrapi.livesight.mapper;

import com.arplanets.corexrapi.livesight.model.dto.OrgDto;
import com.arplanets.corexrapi.livesight.model.po.Org;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrgMapper {

    OrgDto orgPoToOrgDto(Org org);
}
