package com.core.G_O.DTO;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequestDTO {

    @NotBlank(message = "El campo session_id no puede estar en blanco")
    private String sessionId;

    @NotBlank(message = "El campo customer_phone no puede estar en blanco")
    @Pattern(regexp = "^[0-9]{10}$", message = "El numero de telefono solo debe tener 10 dígitos y sin prefijo. Ej: 3995445666")
    private String customerPhone;

    @NotBlank(message = "El campo customer_name no puede estar en blanco")
    @Size(max = 100, message = "La longitud máxima es de 100 caracteres")
    private String customerName;

    @NotBlank(message = "El campo appliance_type no puede estar en blanco")
    private String applianceType;

    @NotBlank(message = "La descripción del problema es obligatoria")
    private String problemDescription;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotNull(message = "La fecha de la cita es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate appointmentDate;

    @NotNull(message = "La hora de la cita es obligatoria")
    private LocalTime appointmentTime;

    private String status;

    // IDs en lugar de objetos completos, para que el cliente mande solo los foreign keys
    private Long technicianId;

    @NotNull(message = "El id del negocio es obligatorio")
    private Long businessId;

    private BigDecimal paid;
}
