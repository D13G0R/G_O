/*package com.core.G_O.controllers;

import com.core.G_O.DTO.BusinessConfigurationUpdateDTO;
import com.core.G_O.models.BusinessContext;
import com.core.G_O.service.BusinessConfigurationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business-configuration")
@CrossOrigin(origins = "*")
public class BusinessConfigurationAPI {

    private final BusinessConfigurationService businessConfigurationService;

    public BusinessConfigurationAPI(BusinessConfigurationService businessConfigurationService) {
        this.businessConfigurationService = businessConfigurationService;
    }

    @GetMapping
    public ResponseEntity<BusinessConfigurationUpdateDTO> getBusinessConfiguration(
            @AuthenticationPrincipal BusinessContext authenticatedBusiness) {
        return ResponseEntity.ok(businessConfigurationService.getBusinessConfiguration(
                authenticatedBusiness.getBusinessId()));
    }

    @PatchMapping
    public ResponseEntity<Void> updateBusinessConfiguration(
            @Valid @RequestBody BusinessConfigurationUpdateDTO update,
            @AuthenticationPrincipal BusinessContext authenticatedBusiness) {
        businessConfigurationService.updateBusinessConfiguration(
                authenticatedBusiness.getBusinessId(), update);
        return ResponseEntity.noContent().build();
    }
}*/
