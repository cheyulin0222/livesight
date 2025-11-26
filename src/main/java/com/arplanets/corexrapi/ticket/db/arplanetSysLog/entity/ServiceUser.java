package com.arplanets.corexrapi.ticket.db.arplanetSysLog.entity;

import jakarta.persistence.*; // (重要) 引入 @GeneratedValue 和 @GenerationType
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "service_users") // 您的資料表名稱 (正確)
public class ServiceUser {

    /**
     * (*** 修正 ***)
     * 1. 這是真正的主鍵 (Primary Key)
     * 2. 欄位名稱是 'uid'
     * 3. @GeneratedValue 表示它是自動增長的
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid")
    private Long uid;

    @Column(name = "auth_id")
    private String authTypeId;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "auth_type")
    private String authType;
}