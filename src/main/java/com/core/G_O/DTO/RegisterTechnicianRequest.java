package com.core.G_O.DTO;

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
    @Pattern(regexp = "^[0-9]{10,15}$", message = "El numero telefonico solo debe contener numeros y tener una longitud entre 10 y 15")
    private String phoneNumber;

    @Email(message = "Debes ingresar una direccion de correo valida")
    @NotBlank(message = "La direccion de correo es obligatoria")
    private String email;

    @NotBlank(message = "El campo idCard (Numero de cedula) no puede estar en blanco")
    @Size(min = 6, max = 16, message = "El campo idCard (Numero de cedula) debe tener entre 6 y 16 caracteres de longitud")
    private String idCard;

    private String photo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;
}
