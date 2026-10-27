package com.core.G_O.service;

import com.core.G_O.DTO.RegisterRequest;
import com.core.G_O.models.Business;
import com.core.G_O.models.Role;
import com.core.G_O.models.Technician;
import com.core.G_O.models.User;
import com.core.G_O.repositories.BusinessRepository;
import com.core.G_O.repositories.RoleRepository;
import com.core.G_O.repositories.TechnicianRepository;
import com.core.G_O.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    UserRepository userRepository;
    BusinessRepository businessRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    TechnicianRepository technicianRepository;

    public AuthService(UserRepository userRepository, BusinessRepository businessRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, TechnicianRepository technicianRepository ) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.technicianRepository = technicianRepository;

    }

    @Transactional
    public String registerBusinessAndUser(RegisterRequest request) {

        String message;

        message = validationRecordDataExistence(request);

        Role role = roleRepository.findById(1L).orElseThrow(() -> new RuntimeException("No existe el rol con id 1"));

        User user = User.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .idCard(request.getIdCard())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .role(role).build();

        User savedUser = userRepository.save(user);

        Business business = Business.builder()
                .businessName(request.getBusinessName())
                .userOwner(savedUser)
                .businessPhoneNumber(request.getBusinessPhoneNumber()).build();

        businessRepository.save(business);

        if(!message.isEmpty()){
            return message;
        }

        return "Usuario, Tecnico y Negocio registrados correctamente";

}

private String validationRecordDataExistence(RegisterRequest request) {

    if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
        return "Error: Ya existe un usuario registrado con ese numero de telefono";
    };
    if (userRepository.existsByEmail(request.getEmail())) {
        return "Error: Ya existe un usuario registrado con ese email";
    };
    if (businessRepository.existsByBusinessName(request.getBusinessName())) {
        return "Error: Ya existe un negocio  registrado con ese nombre.";
    };
    if (businessRepository.existsByBusinessPhoneNumber(request.getBusinessPhoneNumber())){
        return "Error: Ya existe un negocio  registrado con ese numero de telfono";
    }
    else {
        return "";
    }
}

}
