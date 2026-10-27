package com.core.G_O.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianResponseDTO {

    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String idCard;
    private String photo;
    private Long businessId;
    private Long userId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
