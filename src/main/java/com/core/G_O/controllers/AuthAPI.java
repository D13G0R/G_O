package com.core.G_O.controllers;

import com.core.G_O.DTO.RegisterRequest;
import com.core.G_O.models.Business;
import com.core.G_O.models.User;
import com.core.G_O.repositories.BusinessRepository;
import com.core.G_O.repositories.UserRepository;
import com.core.G_O.service.AuthService;
import com.core.G_O.service.JwtService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("No existe un usuario con el correo %s".formatted(email)));

        if (!user.isActive()) {
            return ResponseEntity.status(403).body("Cuenta suspendida por falta de pago");
        }

        Business business = businessRepository.findByUserOwner(user)
                .orElseThrow(() -> new RuntimeException("No hay un negocio asociado a este usuario") );

        String token = jwtService.generateToken(user.getId(), user.getFullName(), email, business.getId());

        return ResponseEntity.ok(Map.of(
            "token", token
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {

        LOG.info("@register API> Start register of data: {}", request);

        String message = authService.registerBusinessAndUser(request);

        return ResponseEntity.status(200).body(Map.of("message", message));
    }
}