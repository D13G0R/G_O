package com.core.G_O.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    @NotNull(message = "El campo businessId es obligatorio")
    private Long businessId;

    @Column(name = "session_id", nullable = false)
    @NotBlank(message = "El campo session_id no puede estar en blanco")
    private String sessionId;

    @Column(name = "customer_phone", nullable = false, length = 10)
    @NotBlank(message = "El campo customer_phone no puede estar en blanco")
    @Pattern(regexp = "^[0-9]{10}$", message = "El numero de telefono solo debe tener 10 dígitos y sin prefijo. Ej: 3995445666")
    private String customerPhone;

    @Column(name = "customer_name", nullable = false, length = 100)
    @NotBlank(message = "El campo customer_name no puede estar en blanco")
    @Size(max = 100, message = "La longitud máxima es de 100 caracteres")
    private String customerName;

    @Column(name = "appliance_type", nullable = false, length = 50)
    @NotBlank(message = "El campo appliance_type no puede estar en blanco")
    private String applianceType;

    @Column(name = "problem_description", columnDefinition = "TEXT")
    @NotBlank(message = "La descripción del problema es obligatoria")
    private String problemDescription;

    @Column(nullable = false)
    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @Column(name = "appointment_date", nullable = false)
    @NotNull(message = "La fecha de la cita es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate appointmentDate;

    @NotNull(message = "La hora de la cita es obligatoria")
    private LocalTime appointmentTime;

    @Column(length = 20)
    private String status = "scheduled";

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null)
            createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
