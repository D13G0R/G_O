package com.core.G_O.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor
public class BusinessConfigurationUpdateDTO {

    private List<@NotBlank(message = "El servicio no puede estar vacío") String> services;

    private List<@NotBlank(message = "La zona no puede estar vacía") String> coverageAreas;

    private Map<String, String> workingHours;

    private List<@NotBlank(message = "La regla no puede estar vacía") String> businessRules;

    private String systemPrompt;

    @Min(value = 1, message = "Debe permitir al menos una cita simultánea")
    private Integer maxParallelAppointments;

    public boolean isThereAnullObject () {
        return Stream.of(services, coverageAreas, workingHours, businessRules, systemPrompt, maxParallelAppointments).allMatch(Objects::isNull);
    }
}
