package com.allobank.splitbill.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "participants")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_group_id", nullable = false)
    private BillGroup billGroup;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int position;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Participant(BillGroup billGroup, String name, int position) {
        this.billGroup = billGroup;
        this.name = name;
        this.position = position;
    }
}
