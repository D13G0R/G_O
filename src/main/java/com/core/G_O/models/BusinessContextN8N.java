package com.core.G_O.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "business_context_n8n")
public class BusinessContextN8N {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "services", columnDefinition = "jsonb")
    private List<String> services;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coverage_areas", columnDefinition = "jsonb")
    private List<String> coverageAreas;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "working_hours", columnDefinition = "jsonb")
    private Map<String, String> workingHours;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "business_rules", columnDefinition = "jsonb")
    private List<String> businessRules;

    @Column(name = "system_prompt")
    private String systemPrompt;

    @Column(name = "max_parallel_appointments")
    private Integer maxParallelAppointments;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
}









