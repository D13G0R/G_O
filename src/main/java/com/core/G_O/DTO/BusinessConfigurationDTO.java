package com.core.G_O.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessConfigurationDTO {

    @NotBlank(message = "El nombre del negocio es obligatorio")
    private String businessName;

    @NotBlank(message = "El nombre del propietario es obligatorio")
    private String ownerName;

    @NotEmpty(message = "Debe existir al menos un servicio")
    private List<@NotBlank(message = "El servicio no puede estar vacío") String> services;

    @NotEmpty(message = "Debe existir al menos una zona de cobertura")
    private List<@NotBlank(message = "La zona no puede estar vacía") String> coverageAreas;

    private Map<String, String> workingHours;

    private List<@NotBlank(message = "La regla no puede estar vacía") String> businessRules;

    private String systemPrompt;

    @Min(value = 1, message = "Debe permitir al menos una cita simultánea")
    private Integer maxParallelAppointments;
}