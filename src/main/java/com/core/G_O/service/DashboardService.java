package com.core.G_O.service;

import com.core.G_O.DTO.DashboardDTOs.DashboardSummaryDTO;
import com.core.G_O.repositories.DashboardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class DashboardService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Bogota");

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public DashboardSummaryDTO getSummary(Long businessId) {

        LocalDate today = LocalDate.now(BUSINESS_ZONE);

        OffsetDateTime dayStart = today.atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();

        OffsetDateTime monthStart = today.withDayOfMonth(1)
                .atStartOfDay(BUSINESS_ZONE)
                .toOffsetDateTime();

        OffsetDateTime now = OffsetDateTime.now(BUSINESS_ZONE);

        return dashboardRepository.getSummary(businessId, today, dayStart, monthStart, now);
        
    }
}
