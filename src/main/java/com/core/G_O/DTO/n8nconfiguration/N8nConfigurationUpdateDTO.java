package com.core.G_O.DTO.n8nconfiguration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class N8nConfigurationUpdateDTO {
    private List<@NotBlank(message = "El servicio no puede estar vacío") String> services;
    private List<@NotBlank(message = "La zona no puede estar vacía") String> coverageAreas;
    private Map<String, String> workingHours;
    private List<@NotBlank(message = "La regla no puede estar vacía") String> businessRules;
    private String systemPrompt;
    @Min(value = 1, message = "Debe permitir al menos una cita simultánea")
    private Integer maxParallelAppointments;
}
