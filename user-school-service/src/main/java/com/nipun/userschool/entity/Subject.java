package com.nipun.userschool.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "subjects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code; // e.g. "MATH_GRADE_6", "SCI_GRADE_7"

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;
}
