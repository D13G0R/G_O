package com.core.G_O.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Builder
@Table(name = "business_context")
@NoArgsConstructor
@AllArgsConstructor
public class BusinessContext implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "phone_number_id", unique = true, nullable = false)
    private String phoneNumberId;

    @Column(name = "business_name", nullable = false)
    private String businessName;

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "max_parallel_appointments")
    private Integer maxParallelAppointments;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    // ==============================
    // Métodos obligatorios de UserDetails
    // ==============================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.phoneNumberId; // Login con phone_number_id
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}