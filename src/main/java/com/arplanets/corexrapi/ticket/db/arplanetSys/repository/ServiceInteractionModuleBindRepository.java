package com.arplanets.corexrapi.ticket.db.arplanetSys.repository;

import com.arplanets.corexrapi.ticket.db.arplanetSys.entity.ServiceInteractionModuleBind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceInteractionModuleBindRepository extends JpaRepository<ServiceInteractionModuleBind, Long> {

    Optional<ServiceInteractionModuleBind> findFirstByInteractionId(long interactionId);
}
