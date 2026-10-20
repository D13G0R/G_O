package com.core.G_O.utils; // O el paquete donde decidas guardarlo

import com.core.G_O.DTO.AppointmentsDTOs.AppointmentResponseDTO;
import com.core.G_O.models.Appointment;

public class AppointmentMapper {

    // Método estático para mapear directamente desde la entidad
    public static AppointmentResponseDTO fromEntity(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .businessId(appointment.getBusiness().getId())
                .sessionId(appointment.getSessionId())
                .customerPhone(appointment.getCustomerPhone())
                .customerName(appointment.getCustomerName())
                .applianceType(appointment.getApplianceType())
                .problemDescription(appointment.getProblemDescription())
                .address(appointment.getAddress())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .status(appointment.getStatus())
                .technicianId(appointment.getTechnician().getId())
                .paid(appointment.getPaid())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
