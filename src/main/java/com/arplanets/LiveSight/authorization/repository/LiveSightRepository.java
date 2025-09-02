package com.arplanets.LiveSight.authorization.repository;

import com.arplanets.LiveSight.authorization.model.po.LiveSightPo;

import java.util.Optional;

public interface LiveSightRepository {

    LiveSightPo create(LiveSightPo liveSight);

    Optional<LiveSightPo> findById(String liveSightId);
}
