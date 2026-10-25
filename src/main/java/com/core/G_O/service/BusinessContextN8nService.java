package com.core.G_O.service;

import com.core.G_O.DTO.n8nconfiguration.N8nConfigurationDTO;
import com.core.G_O.DTO.n8nconfiguration.N8nConfigurationUpdateDTO;
import com.core.G_O.models.Business;
import com.core.G_O.models.BusinessContextN8N;
import com.core.G_O.repositories.BusinessContextN8NRepository;
import com.core.G_O.repositories.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessContextN8nService {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessContextN8nService.class);

    private final BusinessContextN8NRepository businessContextN8NRepository;
    private final BusinessRepository businessRepository;
    private final JwtService jwtService;

    private static final String BEARER_PREFIX = "Bearer ";


    @Transactional(readOnly = true)
    public N8nConfigurationDTO getN8nConfiguration(Long businessId) {
        LOG.info("START: Getting N8n configuration for businessId: {}", businessId);

        Optional<BusinessContextN8N> configuration = businessContextN8NRepository
                .findByBusiness_Id(businessId);

        if (configuration.isEmpty()) {
            LOG.info("END: No N8n configuration found for businessId: {}. Returning empty configuration",
                    businessId);
            return N8nConfigurationDTO.builder().build();
        }

        N8nConfigurationDTO dto = mapToDto(configuration.get());

        LOG.info("END: N8n configuration retrieved successfully for businessId: {}", businessId);
        return dto;
    }

    @Transactional
    public void updateN8nConfiguration(Long businessId, N8nConfigurationUpdateDTO updateDTO) {
        LOG.info("START: Updating N8n configuration for businessId: {}", businessId);

        BusinessContextN8N configuration = businessContextN8NRepository
                .findByBusiness_Id(businessId)
                .orElseGet(() -> createConfiguration(businessId));

        applyUpdates(configuration, updateDTO);
        configuration.setUpdatedAt(LocalDateTime.now());
        businessContextN8NRepository.save(configuration);

        LOG.info("END: N8n configuration updated successfully for businessId: {}", businessId);
    }

    private BusinessContextN8N createConfiguration(Long businessId) {
        LOG.info("No existing N8n configuration for businessId: {}. Creating a new one", businessId);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No existe el negocio solicitado para asociar la configuración N8n"));

        return BusinessContextN8N.builder()
                .business(business)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void applyUpdates(BusinessContextN8N configuration, N8nConfigurationUpdateDTO updateDTO) {
        Optional.ofNullable(updateDTO.getServices()).ifPresent(configuration::setServices);
        Optional.ofNullable(updateDTO.getCoverageAreas()).ifPresent(configuration::setCoverageAreas);
        Optional.ofNullable(updateDTO.getWorkingHours()).ifPresent(configuration::setWorkingHours);
        Optional.ofNullable(updateDTO.getBusinessRules()).ifPresent(configuration::setBusinessRules);
        Optional.ofNullable(updateDTO.getSystemPrompt()).ifPresent(configuration::setSystemPrompt);
        Optional.ofNullable(updateDTO.getMaxParallelAppointments())
                .ifPresent(configuration::setMaxParallelAppointments);
    }

    private N8nConfigurationDTO mapToDto(BusinessContextN8N configuration) {
        return N8nConfigurationDTO.builder()
                .services(configuration.getServices())
                .coverageAreas(configuration.getCoverageAreas())
                .workingHours(configuration.getWorkingHours())
                .businessRules(configuration.getBusinessRules())
                .systemPrompt(configuration.getSystemPrompt())
                .maxParallelAppointments(configuration.getMaxParallelAppointments())
                .build();
    }

    public Long resolveBusinessId(String authorizationHeader) {
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
