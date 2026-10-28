package com.core.G_O.controllers;

import com.core.G_O.DTO.DashboardDTOs.DashboardSummaryDTO;
import com.core.G_O.Exception.AtencIAException;
import com.core.G_O.models.User;
import com.core.G_O.service.DashboardService;
import com.core.G_O.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardAPI {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardAPI.class);

    private final DashboardService dashboardService;
    private final JwtService jwtService;

    public DashboardAPI(DashboardService dashboardService, JwtService jwtService) {
        this.dashboardService = dashboardService;
        this.jwtService = jwtService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @AuthenticationPrincipal User user,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String header) {

        requireOwner(user);

        Long businessId = jwtService.getBusinessId(header.substring(7));

        LOG.info("@getDashboardSummary API > Getting summary for business {}", businessId);

        return ResponseEntity.ok(dashboardService.getSummary(businessId));
    }

    private void requireOwner(User user) {
        if (user == null || user.getRole() == null
                || !"owner".equalsIgnoreCase(user.getRole().getRolName())) {
            throw new AtencIAException(HttpStatus.FORBIDDEN, "Solo el dueño puede consultar el dashboard");
        }
    }
}
