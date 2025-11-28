package com.arplanets.corexrapi.livesight.service;

import com.arplanets.corexrapi.livesight.model.dto.OrgDto;

public interface OrgService {

    OrgDto findByOrgId(String orgId);

}
