package com.core.G_O.DTO.PaymentsDTOs;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReviewResponseDTO {

    private Long id;
    private Long appointmentId;
    private Long businessId;
    private Long technicianId;
    private String technicianName;
    private String technicianPhone;
    private String customerName;
    private String customerPhone;
    private String address;
    private String service;
    private String problemDescription;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private BigDecimal amount;
    private String method;
    private String reference;
    private String status;
    private String observation;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByName;
    private boolean evidenceAvailable;
    private String evidenceContentType;
    private String evidenceUrl;
}
