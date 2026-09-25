package com.core.G_O.controllers;

import com.core.G_O.DTO.RegisterRequest;
import com.core.G_O.models.BusinessContext;
import com.core.G_O.repositories.BusinessRepository;
import com.core.G_O.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private BusinessRepository repository;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String phone = request.get("phoneNumber");
        String password = request.get("password");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(phone, password));

        BusinessContext business = repository.findByPhoneNumberId(phone).orElseThrow();

        if (!business.isActive()) {
            return ResponseEntity.status(403).body("Cuenta suspendida por falta de pago");
        }

        String token = jwtService.generateToken(phone, business.getBusinessId(), business.getFullOwnerName(), business.getEmail(), business.getBusinessName(), business.getPhoneNumberId());

        return ResponseEntity.ok(Map.of(
            "token", token
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // 1. Verificar si el número ya existe
        if (repository.existsByPhoneNumberId(request.getPhoneNumberId())) {
            return ResponseEntity.badRequest().body("Error: El número de teléfono ya está registrado.");
        }

        // 2. Crear el nuevo objeto BusinessContext
        BusinessContext newBusiness = new BusinessContext();
        newBusiness.setPhoneNumberId(request.getPhoneNumberId());
        newBusiness.setBusinessName(request.getBusinessName());
        newBusiness.setFullOwnerName(request.getOwnerName());
        newBusiness.setEmail(request.getEmail());

        // 3. ENCRIPTAR la contraseña antes de guardar
        newBusiness.setPassword(passwordEncoder.encode(request.getPassword()));

        // 4. Por defecto el negocio empieza activo
        newBusiness.setActive(true);

        // 5. Guardar en la DB
        repository.save(newBusiness);

        return ResponseEntity.ok(Map.of("message", "Negocio registrado exitosamente"));
    }
}