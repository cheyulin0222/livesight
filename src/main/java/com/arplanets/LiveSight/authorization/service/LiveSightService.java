package com.arplanets.LiveSight.authorization.service;

import com.arplanets.LiveSight.authorization.model.dto.LiveSightDto;

public interface LiveSightService {

    LiveSightDto createLiveSight(String orgId, String uuid);
    LiveSightDto getLiveSight(String liveSightId);
    boolean isLiveSightExist(String liveSightId);
}
