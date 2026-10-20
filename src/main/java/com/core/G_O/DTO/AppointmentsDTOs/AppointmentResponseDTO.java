package com.core.G_O.DTO.AppointmentsDTOs;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDTO {

    private Long id;

    private Long businessId;

    private String sessionId;

    private String customerPhone;

    private String customerName;

    private String applianceType;

    private String problemDescription;

    private String address;

    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    private String status;

    private Long technicianId;

    private BigDecimal paid;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}