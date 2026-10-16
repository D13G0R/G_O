package com.core.G_O.service;

import com.core.G_O.DTO.BusinessConfigurationUpdateDTO;
import com.core.G_O.models.BusinessContextN8N;
import com.core.G_O.repositories.BusinessContextN8NRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class BusinessConfigurationService {

    private final BusinessContextN8NRepository businessContextN8NRepository;

    public BusinessConfigurationService(BusinessContextN8NRepository businessContextN8NRepository) {
        this.businessContextN8NRepository = businessContextN8NRepository;
    }

    @Transactional(readOnly = true)
    public BusinessConfigurationUpdateDTO getBusinessConfiguration(Long businessId) {

        BusinessContextN8N business = businessContextN8NRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el negocio solicitado"));

        BusinessConfigurationUpdateDTO dto = new BusinessConfigurationUpdateDTO();
        dto.setServices(business.getServices());
        dto.setCoverageAreas(business.getCoverageAreas());
        dto.setWorkingHours(business.getWorkingHours());
        dto.setBusinessRules(business.getBusinessRules());
        dto.setSystemPrompt(business.getSystemPrompt());
        dto.setMaxParallelAppointments(business.getMaxParallelAppointments());
        return dto;
    }

    @Transactional
    public void updateBusinessConfiguration(Long businessId, BusinessConfigurationUpdateDTO update) {

        if (update.isThereAnullObject()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Debe enviar al menos un campo para actualizar");
        }

        BusinessContextN8N business = businessContextN8NRepository.findById(businessId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe el negocio solicitado"));

        validationNullBusinessConfigurationData(update, business);
        // JPA persiste los cambios detectados al cerrar la transacción (dirty checking).
    }

    private void validationNullBusinessConfigurationData (BusinessConfigurationUpdateDTO update, BusinessContextN8N business) {

        if (update == null) return;

        Optional.ofNullable(update.getServices()).ifPresent(service -> business.setServices(service)); //version lambda (se recomienda la de method references)
        Optional.ofNullable(update.getCoverageAreas()).ifPresent(business::setCoverageAreas);
        Optional.ofNullable(update.getWorkingHours()).ifPresent(business::setWorkingHours);
        Optional.ofNullable(update.getBusinessRules()).ifPresent(business::setBusinessRules);
        Optional.ofNullable(update.getSystemPrompt()).ifPresent(business::setSystemPrompt);
        Optional.ofNullable(update.getMaxParallelAppointments()).ifPresent(business::setMaxParallelAppointments);

    }
}
