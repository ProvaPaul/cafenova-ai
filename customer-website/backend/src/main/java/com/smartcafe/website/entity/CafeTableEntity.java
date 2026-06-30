package com.smartcafe.website.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cafe_tables")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CafeTableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, length = 10)
    private String tableNumber;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "location", length = 50)
    private String location;

    @Column(name = "status", length = 20)
    private String status = "AVAILABLE";
}
