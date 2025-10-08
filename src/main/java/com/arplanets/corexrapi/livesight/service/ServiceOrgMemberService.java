package com.arplanets.corexrapi.livesight.service;

import com.arplanets.corexrapi.livesight.model.dto.ServiceOrgMemberDto;

public interface ServiceOrgMemberService {

    ServiceOrgMemberDto findByOrgIdAndUuid(String orgId, String uuid);
}
