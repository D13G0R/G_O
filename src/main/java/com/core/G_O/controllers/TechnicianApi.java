package com.core.G_O.controllers;

import com.core.G_O.DTO.RegisterTechnicianRequest;
import com.core.G_O.DTO.TechnicianResponseDTO;
import com.core.G_O.Exception.AtencIAException;
import com.core.G_O.models.User;
import com.core.G_O.service.JwtService;
import com.core.G_O.service.TechnicianService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/technicians")
public class TechnicianApi {

    private static final Logger LOG = LoggerFactory.getLogger(TechnicianApi.class);

    private final TechnicianService technicianService;
    private final JwtService jwtService;

    public TechnicianApi(TechnicianService technicianService, JwtService jwtService) {
        this.technicianService = technicianService;
        this.jwtService = jwtService;
    }

    @GetMapping("/get")
    public ResponseEntity<List<TechnicianResponseDTO>> getTechnicians(
            @AuthenticationPrincipal User user,
            @RequestHeader("Authorization") String header) {

        LOG.info("@getTechnicians API > Start api to get all technicians");

        if (user == null || user.getRole() == null
                || !"owner".equalsIgnoreCase(user.getRole().getRolName())) {
            throw new AtencIAException(HttpStatus.FORBIDDEN, "Solo el dueño puede consultar los técnicos");
        }

        Long businessId = jwtService.getBusinessId(header.substring(7));

        List<TechnicianResponseDTO> technicians = technicianService.getTechnicians(businessId);

        LOG.info("@getTechnicians API > Technicians retrieved: {}", technicians.size());

        return ResponseEntity.ok(technicians);
    }

    @PostMapping("/register")
    public ResponseEntity<TechnicianResponseDTO> registerTechnician(
            @Valid @RequestBody RegisterTechnicianRequest request,
            @AuthenticationPrincipal User owner,
            @RequestHeader("Authorization") String header) {

        LOG.info("@registerTechnician API > Start register for technician with email {}", request.getEmail());

        if (owner == null || owner.getRole() == null
                || !"owner".equalsIgnoreCase(owner.getRole().getRolName())) {
            throw new AtencIAException(HttpStatus.FORBIDDEN, "Solo el dueño puede registrar técnicos");
        }

        Long businessId = jwtService.getBusinessId(header.substring(7));

        TechnicianResponseDTO response = technicianService.registerTechnician(request, owner, businessId);

        LOG.info("@registerTechnician API > Technician {} registered", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
