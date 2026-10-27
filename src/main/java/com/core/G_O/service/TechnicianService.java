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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class TechnicianService {

    private static final Logger LOG = LoggerFactory.getLogger(TechnicianService.class);

    private final TechnicianRepository technicianRepository;
    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TechnicianPhotoStorageService photoStorageService;

    public TechnicianService(TechnicianRepository technicianRepository,
                             UserRepository userRepository,
                             BusinessRepository businessRepository,
                             RoleRepository roleRepository,
                             PasswordEncoder passwordEncoder,
                             TechnicianPhotoStorageService photoStorageService) {
        this.technicianRepository = technicianRepository;
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.photoStorageService = photoStorageService;
    }

    public List<TechnicianResponseDTO> getTechnicians(Long businessId) {
        LOG.info("@getTechnicians SERV > Start service to get all technicians with de businessID {}", businessId);

        List<Technician> technicians = technicianRepository.findByWorkAt_Id(businessId).orElse(List.of());

        LOG.info("@getTechnicians SERV > The service to retrieve all technicians has concluded");

        return technicians.stream().map(TechnicianService::toResponse).toList();
    }

    @Transactional
    public TechnicianResponseDTO registerSelf(User owner, Long businessId, MultipartFile photo) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new AtencIAException(HttpStatus.NOT_FOUND, "No existe el negocio asociado"));

        Technician existingTechnician = technicianRepository.findByUser_Id(owner.getId()).orElse(null);

        if (existingTechnician != null
                && existingTechnician.getWorkAt() != null
                && !existingTechnician.getWorkAt().getId().equals(businessId)) {
            throw new AtencIAException(HttpStatus.CONFLICT,
                    "Tu ficha de técnico está vinculada a otro negocio");
        }

        String savedPhoto = photoStorageService.save(photo, businessId);

        try {
            if (existingTechnician != null) {
                String previousPhoto = existingTechnician.getPhoto();

                if (savedPhoto != null) {
                    existingTechnician.setPhoto(savedPhoto);
                }

                if (existingTechnician.getWorkAt() == null) {
                    existingTechnician.setWorkAt(business);
                }

                Technician saved = technicianRepository.save(existingTechnician);

                if (savedPhoto != null) {
                    photoStorageService.delete(previousPhoto);
                }

                LOG.info("@registerTechnicianSelf SERV > Owner technician {} updated for business {}",
                        saved.getId(), businessId);

                return toResponse(saved);
            }

            Technician technician = Technician.builder()
                    .photo(savedPhoto)
                    .user(owner)
                    .workAt(business)
                    .build();

            Technician saved = technicianRepository.save(technician);

            LOG.info("@registerTechnicianSelf SERV > Owner technician {} registered for business {}",
                    saved.getId(), businessId);

            return toResponse(saved);
        } catch (RuntimeException exception) {
            photoStorageService.delete(savedPhoto);
            throw exception;
        }
    }

    @Transactional
    public TechnicianResponseDTO registerTechnician(RegisterTechnicianRequest request,
                                                    MultipartFile photo,
                                                    Long businessId) {

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new AtencIAException(HttpStatus.NOT_FOUND, "No existe el negocio asociado"));

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AtencIAException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con ese email");
        }

        if (userRepository.existsByIdCard(request.getIdCard())) {
            throw new AtencIAException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con esa cédula");
        }

        Role technicianRole = roleRepository.findByRolName("technician")
                .orElseThrow(() -> new AtencIAException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No existe el rol technician en la base de datos"));

        String savedPhoto = photoStorageService.save(photo, businessId);

        try {
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

            Technician technician = Technician.builder()
                    .photo(savedPhoto)
                    .user(savedUser)
                    .workAt(business)
                    .build();

            Technician saved = technicianRepository.save(technician);

            LOG.info("@registerTechnician SERV > Technician {} registered for business {}", saved.getId(), businessId);

            return toResponse(saved);
        } catch (RuntimeException exception) {
            photoStorageService.delete(savedPhoto);
            throw exception;
        }
    }

    public static TechnicianResponseDTO toResponse(Technician technician) {
        User technicianUser = technician.getUser();

        return TechnicianResponseDTO.builder()
                .id(technician.getId())
                .fullName(technicianUser == null ? null : technicianUser.getFullName())
                .phoneNumber(technicianUser == null ? null : technicianUser.getPhoneNumber())
                .email(technicianUser == null ? null : technicianUser.getEmail())
                .idCard(technicianUser == null ? null : technicianUser.getIdCard())
                .photo(technician.getPhoto())
                .businessId(technician.getWorkAt() == null ? null : technician.getWorkAt().getId())
                .userId(technicianUser == null ? null : technicianUser.getId())
                .active(technicianUser == null || technicianUser.isActive())
                .createdAt(technician.getCreatedAt())
                .updatedAt(technician.getUpdatedAt())
                .build();
    }
}
