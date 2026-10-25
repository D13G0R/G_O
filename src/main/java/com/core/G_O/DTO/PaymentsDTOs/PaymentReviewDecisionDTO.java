package com.core.G_O.DTO.PaymentsDTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentReviewDecisionDTO {

    @NotBlank(message = "El estado de la revisión es obligatorio")
    private String status;

    private String observation;
}
