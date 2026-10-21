package com.core.G_O.Exception;

import com.core.G_O.DTO.ResponseDTOs.ErrorResponseDTO;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(AtencIAException.class)
    public ResponseEntity<ErrorResponseDTO> response (AtencIAException exception) {
// Use la manera antigua para practicar y poder usar cosas diferentes (no todo es builder)
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(exception.getMessage(), exception.getStatus());

        return ResponseEntity.status(exception.getStatus()).body(errorResponseDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> responseErrorUnexpected (Exception exception) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseDTO);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> responseIllegalArgument (IllegalArgumentException exception) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(exception.getMessage(), HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDTO);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> responseValidationField (MethodArgumentNotValidException exception) {

        String message = Objects.requireNonNull(exception.getBindingResult().getFieldErrors())
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(" - "));

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(message, HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDTO);
    }
}
