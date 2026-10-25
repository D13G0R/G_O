package com.core.G_O.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "technicians")
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El campo name (Nombre) no puede estar en blanco")
    @Size(min = 5, max = 200, message = "El campo (Nombre) debe tener entre 5 y 200 caracteres de longitud")
    private String fullName;

    @NotNull(message = "El numero de telefono es obligatorio")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "El numero telefonico solo debe contener numeros y tener una longitud entre 11 y 15")
    private String phoneNumber;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "El campo idCard (Numero de cedulda) no puede estar en blanco")
    @Size(min = 6, max = 16, message = "El campo idCard (Numero de cedulda) debe tener entre 6 y 16 caracteres de longitud")
    private String idCard;


    private String photo;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "boss", nullable = false)
    private User boss;

    @ManyToOne(optional = false)
    @JoinColumn(name = "work_at")
    private Business workAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime  updatedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}
