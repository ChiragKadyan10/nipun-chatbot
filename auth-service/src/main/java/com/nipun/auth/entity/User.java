package com.nipun.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // SUPER_ADMIN, SCHOOL_ADMIN, TEACHER, VIEWER

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "school_id")
    private UUID schoolId;
}
