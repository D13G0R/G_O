package com.core.G_O.DTO.AppointmentsDTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StopwatchUpdateDTO {

    @NotBlank(message = "La acción del cronómetro es obligatoria")
    private String action;
}
