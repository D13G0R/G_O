package com.core.G_O.DTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTechnicianRequest {

    @NotBlank(message = "El campo name (Nombre) no puede estar en blanco")
    @Size(min = 5, max = 200, message = "El campo (Nombre) debe tener entre 5 y 200 caracteres de longitud")
    private String fullName;

    @NotNull(message = "El numero de telefono es obligatorio")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "El numero telefonico solo debe contener numeros y tener una longitud entre 11 y 15")
    private String phoneNumber;

    @Email(message = "Debes ingresar una direccion de correo valida")
    @NotNull(message = "La direccion de correo es obligatoria")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message="El campo email(correo) tiene un error")
    private String email;

    @NotBlank(message = "El campo idCard (Numero de cedulda) no puede estar en blanco")
    @Size(min = 6, max = 16, message = "El campo idCard (Numero de cedulda) debe tener entre 6 y 16 caracteres de longitud")
    private String idCard;

    private String photo;

    @NotNull(message = "No se está asociando un jefe a este tecnico")
    private Long boss;

    @NotNull(message = "No se está asociando un negocio a este tecnico")
    private Long workAt;

}
