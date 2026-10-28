package com.core.G_O.DTO.DashboardDTOs;

import java.math.BigDecimal;

public record DashboardSummaryDTO(
        long totalTechnicians,
        long activeTechnicians,
        BigDecimal todayRevenue,
        BigDecimal monthRevenue,
        long todayScheduledAppointments,
        long todayConfirmedAppointments,
        long todayInProgressAppointments,
        long todayPendingPaymentAppointments,
        long todayPaymentReviewAppointments,
        long todayPaymentRejectedAppointments,
        long todayCompletedAppointments,
        long todayCancelledAppointments,
        long todayOtherAppointments,
        long totalTodayAppointments
) {
}
