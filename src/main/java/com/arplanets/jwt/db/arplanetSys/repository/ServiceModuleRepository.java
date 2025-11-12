package com.arplanets.jwt.db.arplanetSys.repository;

import com.arplanets.jwt.db.arplanetSys.entity.ServiceInteractionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceModuleRepository extends JpaRepository<ServiceInteractionModule, Long> {

    Optional<ServiceInteractionModule> findByModuleId(long moduleId);
}