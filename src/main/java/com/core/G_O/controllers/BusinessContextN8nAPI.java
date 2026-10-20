package com.core.G_O.controllers;

import com.core.G_O.DTO.n8nconfiguration.N8nConfigurationDTO;
import com.core.G_O.DTO.n8nconfiguration.N8nConfigurationUpdateDTO;
import com.core.G_O.models.User;
import com.core.G_O.service.BusinessContextN8nService;
import com.core.G_O.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/business-configuration")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BusinessContextN8nAPI {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessContextN8nAPI.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final BusinessContextN8nService businessContextN8nService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<N8nConfigurationDTO> getN8nConfiguration(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        Long businessId = resolveBusinessId(authorizationHeader);
        LOG.info("START: GET N8n configuration request for businessId: {} (user: {})",
                businessId, authenticatedUser.getEmail());

        N8nConfigurationDTO configuration = businessContextN8nService.getN8nConfiguration(businessId);

        LOG.info("END: GET N8n configuration request processed for businessId: {}", businessId);
        return ResponseEntity.ok(configuration);
    }

    @PatchMapping
    public ResponseEntity<Void> updateN8nConfiguration(
            @Valid @RequestBody N8nConfigurationUpdateDTO updateDTO,
            @AuthenticationPrincipal User authenticatedUser,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        Long businessId = resolveBusinessId(authorizationHeader);
        LOG.info("START: PATCH N8n configuration request for businessId: {} (user: {})",
                businessId, authenticatedUser.getEmail());

        businessContextN8nService.updateN8nConfiguration(businessId, updateDTO);

        LOG.info("END: PATCH N8n configuration request processed for businessId: {}", businessId);
        return ResponseEntity.noContent().build();
    }

    private Long resolveBusinessId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token de autorización no proporcionado");
        }

        Long businessId = jwtService.getBusinessId(
                authorizationHeader.substring(BEARER_PREFIX.length()));

        if (businessId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "El token no contiene el business_id");
        }

        return businessId;
    }
}
