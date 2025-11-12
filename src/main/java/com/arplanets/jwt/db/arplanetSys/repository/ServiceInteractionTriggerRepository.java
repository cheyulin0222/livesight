package com.arplanets.jwt.db.arplanetSys.repository;

import com.arplanets.jwt.db.arplanetSys.entity.ServiceInteractionTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceInteractionTriggerRepository extends JpaRepository<ServiceInteractionTrigger, Long> {

    Optional<ServiceInteractionTrigger> findByTriggerTypeAndTriggerTypeId(String triggerType, String triggerTypeId);
}
