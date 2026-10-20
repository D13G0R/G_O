package com.core.G_O.DTO.AppointmentsDTOs;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AppointmentUpdateDTO {
        private Long Id;
        private String customerPhone;
        private String customerName;
        private String applianceType;
        private String problemDescription;
        private String address;
        private LocalDate appointmentDate;
        private LocalTime appointmentTime;
        private String status;
        private Long technicianId; // Solo el ID plano
        private BigDecimal paid;

}
