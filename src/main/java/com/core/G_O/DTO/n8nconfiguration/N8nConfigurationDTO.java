package com.core.G_O.DTO.n8nconfiguration;

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
public class N8nConfigurationDTO {
    private List<String> services;
    private List<String> coverageAreas;
    private Map<String, String> workingHours;
    private List<String> businessRules;
    private String systemPrompt;
    private Integer maxParallelAppointments;
}
