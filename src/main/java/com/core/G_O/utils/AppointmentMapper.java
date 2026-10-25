package com.core.G_O.utils; // O el paquete donde decidas guardarlo

import com.core.G_O.DTO.AppointmentsDTOs.AppointmentResponseDTO;
import com.core.G_O.models.Appointment;
import com.core.G_O.models.PaymentReview;

public class AppointmentMapper {

    // Método estático para mapear directamente desde la entidad
    public static AppointmentResponseDTO fromEntity(Appointment appointment) {
        return fromEntity(appointment, null);
    }

    public static AppointmentResponseDTO fromEntity(Appointment appointment, PaymentReview paymentReview) {
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
                .technicianId(appointment.getTechnician() == null ? null : appointment.getTechnician().getId())
                .paid(appointment.getPaid())
                .paymentStatus(paymentReview == null ? null : paymentReview.getStatus())
                .paymentAmount(paymentReview == null ? null : paymentReview.getAmount())
                .paymentMethod(paymentReview == null ? null : paymentReview.getMethod())
                .paymentObservation(paymentReview == null ? null : paymentReview.getObservation())
                .paymentSubmittedAt(paymentReview == null ? null : paymentReview.getSubmittedAt())
                .paymentReviewedAt(paymentReview == null ? null : paymentReview.getReviewedAt())
                .hasPaymentEvidence(paymentReview != null)
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }
}
