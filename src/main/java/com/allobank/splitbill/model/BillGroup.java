package com.allobank.splitbill.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "bill_groups")
public class BillGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "owner_session_id", nullable = false, length = 255)
    private String ownerSessionId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public BillGroup(String name, String ownerSessionId) {
        this.name = name;
        this.ownerSessionId = ownerSessionId;
        this.currency = "IDR";
    }
}
