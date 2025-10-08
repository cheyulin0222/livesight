package com.arplanets.corexrapi.livesight.mapper;

import com.arplanets.corexrapi.livesight.model.dto.ServiceOrgMemberDto;
import com.arplanets.corexrapi.livesight.model.po.ServiceOrgMember;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceOrgMemberMapper {

    ServiceOrgMemberDto orderPoToOrderDto(ServiceOrgMember serviceOrgMember);
}
