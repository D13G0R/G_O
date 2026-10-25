package com.core.G_O.service;

import com.core.G_O.DTO.RegisterTechnicianRequest;
import com.core.G_O.DTO.TechnicianResponseDTO;
import com.core.G_O.Exception.AtencIAException;
import com.core.G_O.models.Business;
import com.core.G_O.models.Role;
import com.core.G_O.models.Technician;
import com.core.G_O.models.User;
import com.core.G_O.repositories.BusinessRepository;
import com.core.G_O.repositories.RoleRepository;
import com.core.G_O.repositories.TechnicianRepository;
import com.core.G_O.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicianService {

    private static final Logger LOG = LoggerFactory.getLogger(TechnicianService.class);

    private final TechnicianRepository technicianRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public TechnicianService(TechnicianRepository technicianRepository,
                             UserRepository userRepository,
                             BusinessRepository businessRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder) {
        this.technicianRepository = technicianRepository;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<TechnicianResponseDTO> getTechnicians(Long businessId) {
        LOG.info("@getTechnicians SERV > Start service to get all technicians with de businessID {}", businessId);

        List<Technician> technicians = technicianRepository.findByWorkAt_Id(businessId).orElseThrow(
                () -> new AtencIAException(HttpStatus.NOT_FOUND,
                        "No hay tecnicos asociados al negocio con id " + businessId));

        LOG.info("@getTechnicians SERV > The service to retrieve all technicians has concluded");

        return technicians.stream().map(TechnicianService::toResponse).toList();
    }

    @Transactional
    public TechnicianResponseDTO registerTechnician(RegisterTechnicianRequest request,
                                                    User owner,
                                                    Long businessId) {

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new AtencIAException(HttpStatus.NOT_FOUND, "No existe el negocio asociado"));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AtencIAException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con ese email");
        }

        if (userRepository.existsByIdCard(request.getIdCard())) {
            throw new AtencIAException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con esa cédula");
        }

        Technician existingTechnician = technicianRepository.findByEmail(request.getEmail()).orElse(null);

        if (existingTechnician != null) {
            if (existingTechnician.getUser() != null) {
                throw new AtencIAException(HttpStatus.CONFLICT, "Ya existe un técnico registrado con ese email");
            }

            if (existingTechnician.getWorkAt() == null
                    || !existingTechnician.getWorkAt().getId().equals(businessId)) {
                throw new AtencIAException(HttpStatus.CONFLICT,
                        "El técnico existe en otro negocio y no se puede vincular");
            }
        }

        Role technicianRole = roleRepository.findByRolName("technician")
                .orElseThrow(() -> new AtencIAException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No existe el rol technician en la base de datos"));

        User technicianUser = User.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .idCard(request.getIdCard())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .role(technicianRole)
                .build();

        User savedUser = userRepository.save(technicianUser);

        Technician technician;

        if (existingTechnician != null) {
            technician = existingTechnician;
            technician.setUser(savedUser);
            technician.setFullName(request.getFullName());
            technician.setPhoneNumber(request.getPhoneNumber());
            technician.setIdCard(request.getIdCard());
            technician.setPhoto(request.getPhoto());
        } else {
            technician = Technician.builder()
                    .fullName(request.getFullName())
                    .phoneNumber(request.getPhoneNumber())
                    .email(request.getEmail())
                    .idCard(request.getIdCard())
                    .photo(request.getPhoto())
                    .user(savedUser)
                    .boss(owner)
                    .workAt(business)
                    .build();
        }

        Technician saved = technicianRepository.save(technician);

        LOG.info("@registerTechnician SERV > Technician {} registered for business {}", saved.getId(), businessId);

        return toResponse(saved);
    }

    public static TechnicianResponseDTO toResponse(Technician technician) {
        return TechnicianResponseDTO.builder()
                .id(technician.getId())
                .fullName(technician.getFullName())
                .phoneNumber(technician.getPhoneNumber())
                .email(technician.getEmail())
                .idCard(technician.getIdCard())
                .photo(technician.getPhoto())
                .businessId(technician.getWorkAt() == null ? null : technician.getWorkAt().getId())
                .bossId(technician.getBoss() == null ? null : technician.getBoss().getId())
                .userId(technician.getUser() == null ? null : technician.getUser().getId())
                .active(technician.getUser() == null || technician.getUser().isActive())
                .createdAt(technician.getCreatedAt())
                .updatedAt(technician.getUpdatedAt())
                .build();
    }
}
