package com.arplanets.jwt.db.arplanetSysLog.repository;

import com.arplanets.jwt.db.arplanetSysLog.entity.ServiceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceUserRepository extends JpaRepository<ServiceUser, String> {

    /**
     * Spring Data JPA 會自動幫我們產生 SQL:
     * "SELECT COUNT(*) FROM service_users WHERE auth_type_id = ?"
     * * 我們用這個方法來快速檢查 'auth_type_id' 是否存在
     */
    boolean existsByAuthTypeId(String authTypeId);
}