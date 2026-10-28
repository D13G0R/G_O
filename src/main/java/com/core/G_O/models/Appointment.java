package com.core.G_O.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appointments")
public class Appointment {

    public static final String SCHEDULED = "scheduled";
    public static final String CONFIRMED = "confirmed";
    public static final String IN_PROGRESS = "in_progress";
    public static final String PENDING_PAYMENT = "pending_payment";
    public static final String PAYMENT_REVIEW = "payment_review";
    public static final String PAYMENT_REJECTED = "payment_rejected";
    public static final String COMPLETED = "completed";
    public static final String CANCELLED = "cancelled";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "customer_phone", nullable = false, length = 10)
    private String customerPhone;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "appliance_type", nullable = false, length = 50)
    private String applianceType;

    @Column(name = "problem_description", columnDefinition = "TEXT", nullable = false)
    private String problemDescription;

    @Column(nullable = false)
    private String address;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "appointment_time", nullable = false) // Asegúrate de mapear la columna si es necesario
    private LocalTime appointmentTime;

    @Column(length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(optional = true)
    @JoinColumn(name = "technician_id")
    private Technician technician; // 💡 Cambié el nombre de technicianId a technician (es un objeto, no un ID numérico)

    @ManyToOne(optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "paid", precision = 10, scale = 0, nullable = true)
    private BigDecimal paid;

    @Column(name = "start_time")
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}