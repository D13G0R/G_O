package com.core.G_O.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotNull(message = "El numero de telefono es obligatorio")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "El numero telefonico solo debe contener numeros y tener una longitud entre 11 y 15")
    private String phoneNumberId;

    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-])[A-Za-z\\d@$!%*?&_\\-]{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
    )
    private String password;

    @NotBlank(message = "El campo businessName no puede estar en blanco")
    private String businessName;

    @NotBlank(message = "El campo ownerName (Nombre de propietario) no puede estar en blanco")
    @Size(min = 3, max = 60, message = "El campo ownerName (Nombre de propietario) debe tener entre 3 y 60 caracteres de longitud")
    private String ownerName;

    @Email(message = "Debes ingresar una direccion de correo valida")
    @NotNull(message = "La direccion de correo es obligatoria")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$", message="El campo email(correo) tiene un error")
    private String email;
}