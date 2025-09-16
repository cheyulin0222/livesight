package com.arplanets.corexrapi.livesight.service;

import com.arplanets.corexrapi.livesight.model.dto.LiveSightDto;

public interface LiveSightService {

    LiveSightDto createLiveSight(String orgId, String uuid);
    LiveSightDto getLiveSight(String liveSightId);
    boolean isLiveSightExist(String liveSightId);
}
