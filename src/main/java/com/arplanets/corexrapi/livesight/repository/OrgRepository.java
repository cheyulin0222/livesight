package com.arplanets.corexrapi.livesight.repository;

import com.arplanets.corexrapi.livesight.model.po.Org;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgRepository extends JpaRepository<Org, Long> {

    Optional<Org> findByOrgIdAndActiveTrueAndDeletedAtIsNull(String orgId);
}
