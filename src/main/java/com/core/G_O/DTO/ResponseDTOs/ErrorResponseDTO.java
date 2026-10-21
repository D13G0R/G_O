package com.core.G_O.DTO.ResponseDTOs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

//anotaciones
@Getter
@Setter
public class ErrorResponseDTO {

    private String message;
    private HttpStatus status;

    //Si quisiera pudiera usar la anotación @AllArgsConstructor o @NoArgsConstructor
    public ErrorResponseDTO (String message, HttpStatus status){
        this.message = message;
        this.status = status;
    }
    //• En Java, dile siempre anotación.
    //• Si vienes de JavaScript/TypeScript/Python, estás acostumbrado a decirle decorador.
}
