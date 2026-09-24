package com.core.G_O.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "business_context")
@NoArgsConstructor
@AllArgsConstructor
public class BusinessContext implements UserDetails {

    @Id
    @Column(name = "business_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessId;

    @Column(name = "phone_number_id", unique = true, nullable = false)
    private String phoneNumberId;

    @Column(name= "email", unique=true, nullable=false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "owner_name", nullable = false)
    private String fullOwnerName;

    @Column(name = "is_active")
    private boolean isActive = true;

    // Métodos obligatorios de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
    @Override
    public String getPassword() { return this.password; }
    @Override
    public String getUsername() { return this.phoneNumberId; } // Tu login es el teléfono
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return this.isActive; } // Si no paga, isActive es false y no entra
}