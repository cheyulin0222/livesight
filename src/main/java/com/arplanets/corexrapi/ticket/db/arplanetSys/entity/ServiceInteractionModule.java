package com.arplanets.corexrapi.ticket.db.arplanetSys.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "service_interaction_modules") // <-- (1) (重要) 我假設的資料表名稱
public class ServiceInteractionModule {

    // 我們假設 'module_id' 是這張表的主鍵
    @Id
    @Column(name = "module_id") // <-- (2) 欄位名稱
    private long moduleId;

    @Column(name = "module_content_url") // <-- (3) 儲存 JSON 字串的欄位
    private String moduleContentUrl;
}
