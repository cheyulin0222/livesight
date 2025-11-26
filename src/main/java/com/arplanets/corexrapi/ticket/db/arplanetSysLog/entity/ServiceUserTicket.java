package com.arplanets.corexrapi.ticket.db.arplanetSysLog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "service_user_tickets") // 資料表名稱 (正確)
public class ServiceUserTicket {

    /**
     * (*** 修正 ***)
     * 1. 這是主鍵 (Primary Key)，在截圖中是第一欄。
     * 2. 欄位名稱是 'ticket_objid'。
     * 3. 您的 Java 變數名稱 'ticketObjid' 也是正確的，
     * 這樣 'findByTicketObjid' 才能運作。
     */
    @Id
    @Column(name = "ticket_objid")
    private long ticketObjid;

    @Column(name = "ticket_id")
    private long ticketId;
}