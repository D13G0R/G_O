package com.core.G_O.controllers;

import com.core.G_O.DTO.RegisterRequest;
import com.core.G_O.Exception.AtencIAException;
import com.core.G_O.models.Business;
import com.core.G_O.models.Technician;
import com.core.G_O.models.User;
import com.core.G_O.repositories.BusinessRepository;
import com.core.G_O.repositories.TechnicianRepository;
import com.core.G_O.repositories.UserRepository;
import com.core.G_O.service.AuthService;
import com.core.G_O.service.JwtService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthAPI {

    private final Logger LOG = LoggerFactory.getLogger(AuthAPI.class);

     AuthenticationManager authenticationManager;
     JwtService jwtService;
     AuthService authService;

    @Autowired private UserRepository userRepository;
    @Autowired private BusinessRepository businessRepository;
    @Autowired private TechnicianRepository technicianRepository;

    public AuthAPI(AuthenticationManager authenticationManager,
                   JwtService jwtService, AuthService authService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.authService = authService;

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        } catch(BadCredentialsException e) {

            throw new AtencIAException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");

        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("No existe un usuario con el correo %s".formatted(email)));

        if (!user.isActive()) {
            return ResponseEntity.status(403).body("Cuenta suspendida por falta de pago");
        }

        String role = user.getRole() != null ? user.getRole().getRolName() : "owner";

        Technician technician = technicianRepository.findByUser_Id(user.getId())
                .or(() -> technicianRepository.findByEmail(email))
                .orElse(null);

        Business business;

        if ("technician".equalsIgnoreCase(role)) {
            if (technician == null || technician.getWorkAt() == null) {
                throw new AtencIAException(HttpStatus.FORBIDDEN, "El técnico no tiene un negocio asociado");
            }
            business = technician.getWorkAt();
        } else {
            business = businessRepository.findByUserOwner(user)
                    .orElseThrow(() -> new AtencIAException(HttpStatus.NOT_FOUND, "No hay un negocio asociado a este usuario"));
        }

        Long technicianId = technician != null ? technician.getId() : null;

        String token = jwtService.generateToken(
                user.getId(), user.getFullName(), email, business.getId(), role, technicianId);

        return ResponseEntity.ok(Map.of(
            "token", token,
            "role", role,
            "technicianId", technicianId == null ? "" : technicianId
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {

        LOG.info("@register API> Start register of data: {}", request);

        String message = authService.registerBusinessAndUser(request);

        return ResponseEntity.status(200).body(Map.of("message", message));
    }
}