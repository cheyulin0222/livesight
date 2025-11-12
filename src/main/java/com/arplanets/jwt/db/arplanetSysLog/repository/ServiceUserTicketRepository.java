package com.arplanets.jwt.db.arplanetSysLog.repository;

import com.arplanets.jwt.db.arplanetSysLog.entity.ServiceUserTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceUserTicketRepository extends JpaRepository<ServiceUserTicket, Long> {

    Optional<ServiceUserTicket> findByTicketObjid(long ticketObjid);
}