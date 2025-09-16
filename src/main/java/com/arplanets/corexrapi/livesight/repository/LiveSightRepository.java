package com.arplanets.corexrapi.livesight.repository;

import com.arplanets.corexrapi.livesight.model.po.LiveSightPo;

import java.util.Optional;

public interface LiveSightRepository {

    LiveSightPo create(LiveSightPo liveSight);

    Optional<LiveSightPo> findById(String liveSightId);
}
