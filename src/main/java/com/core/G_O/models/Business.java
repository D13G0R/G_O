package com.core.G_O.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Builder.Default
    @Column(name = "active")
    private boolean isActive = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_owner", nullable = false)
    private User userOwner;

    @Column(name = "business_phone_number")
    private String businessPhoneNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void OnCreated(){
        this.createdAt = LocalDateTime.now();
    }


}
