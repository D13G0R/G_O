package com.core.G_O.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El campo name (Nombre) no puede estar en blanco")
    @Size(min = 5, max = 200, message = "El campo name (Nombre) debe tener entre 5 y 200 caracteres de longitud")
    private String fullName;

    @NotNull(message = "El numero de telefono es obligatorio")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "El numero telefonico solo debe contener numeros y tener una longitud entre 11 y 15")
    private String phoneNumber;

    @Email(message = "Debes ingresar una direccion de correo valida")
    @NotNull(message = "La direccion de correo es obligatoria")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message="El campo email(correo) tiene un error")
    private String email;

    @NotBlank(message = "el numero de cedula es obligatorio")
    @Size(min = 6, max = 16, message = "el numero de cedula debe tener entre 6 y 16 numeros consecutivos sin espacios ni signos.")
    private String idCard;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-])[A-Za-z\\d@$!%*?&_\\-]{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String password;

    @NotBlank(message = "El campo businessName no puede estar en blanco")
    @Size(min = 1, max = 200, message = "El campo businessName (Nombre de negocio) debe tener entre 1 y 200 caracteres de longitud")
    private String businessName;

    @NotNull(message = "El numero de telefono del negocio es obligatorio")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "El numero telefonico solo debe contener numeros y tener una longitud entre 11 y 15")
    private String businessPhoneNumber;




}