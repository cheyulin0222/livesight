package com.arplanets.corexrapi.livesight.repository;

import com.arplanets.corexrapi.livesight.model.po.ServiceOrgMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceOrgMemberRepository extends JpaRepository<ServiceOrgMember, Long> {

    Optional<ServiceOrgMember> findByOrgIdAndUuid(String orgId, String uuid);
}
