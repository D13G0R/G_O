package com.core.G_O.service;

import com.core.G_O.DTO.AppointmentsDTOs.AppointmentRequestDTO;
import com.core.G_O.DTO.AppointmentsDTOs.AppointmentResponseDTO;
import com.core.G_O.DTO.AppointmentsDTOs.AppointmentUpdateDTO;
import com.core.G_O.models.Appointment;
import com.core.G_O.models.Business;
import com.core.G_O.models.Technician;
import com.core.G_O.repositories.AppointmentRepository;
import com.core.G_O.repositories.BusinessRepository;
import com.core.G_O.repositories.TechnicianRepository;
import com.core.G_O.utils.AppointmentMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;


@Service
public class AppointmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentService.class);

    AppointmentRepository appointmentRepository;
    TechnicianRepository technicianRepository;
    BusinessRepository businessRepository;
    JwtService jwtService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TechnicianRepository technicianRepository,
                              BusinessRepository businessRepository,
                              JwtService jwtService){

        this.appointmentRepository = appointmentRepository;
        this.technicianRepository = technicianRepository;
        this.businessRepository = businessRepository;
        this.jwtService = jwtService;

    }

    public List<AppointmentResponseDTO> getAppointmentsByBusinessID(Long business_id){

        LOG.info("@getAppointmentsByBusinessID SERV > Stars the service to obtain all the apointments of the business_id");

        List<Appointment> appointments =  appointmentRepository.findByBusinessId(business_id);

        List<AppointmentResponseDTO> response = appointments.stream().map(AppointmentMapper::fromEntity).toList();

        LOG.info("@getAppointmentsByBusinessID SERV > Ends the service, The appointments obtained were {} for the business_id: {}.",
                response, business_id);

        return response;
    }

    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentRequestDTO appointmentRequest, Long businessId){
        LOG.info("@createAppointment SERV > Starts the service to save the appointment");

        Business business = businessRepository.findById(businessId).orElseThrow(() -> new RuntimeException("No hay un negocio al cual asociar esta cita"));
        Technician technician = technicianRepository.findById(appointmentRequest.getTechnicianId()).orElseThrow(() -> new RuntimeException("No existe un tecnico asociado a ese id"));

        Appointment appointment = Appointment.builder()
                .sessionId(appointmentRequest.getSessionId())
                .customerPhone(appointmentRequest.getCustomerPhone())
                .customerName(appointmentRequest.getCustomerName())
                .applianceType(appointmentRequest.getApplianceType())
                .problemDescription(appointmentRequest.getProblemDescription())
                .address(appointmentRequest.getAddress())
                .appointmentDate(appointmentRequest.getAppointmentDate())
                .appointmentTime(appointmentRequest.getAppointmentTime())
                .status(appointmentRequest.getStatus())
                .technician(technician)
                .business(business)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        AppointmentResponseDTO response = AppointmentMapper.fromEntity(saved);

        LOG.info("@createAppointment SERV > Ends the service, saved appointment: {}", saved);
        return response;
    }

    public AppointmentResponseDTO updateAppointment(Long appointmentIdFromPath, AppointmentUpdateDTO updatedAppointment, String token) {

        LOG.info("@updateAppointment SERV > Start update of the appointment with AppointmentId {}", appointmentIdFromPath);

        Long businessId = jwtService.getBusinessId(token);

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("No hay negocio para validar que la cita sea válida"));

        if (appointmentIdFromPath == null || updatedAppointment.getId() == null) {
            throw new IllegalArgumentException("El identificador de la cita es null");
        }

        if(!Objects.equals(appointmentIdFromPath, updatedAppointment.getId())){
            throw new RuntimeException("Los ids no coinciden (alerta, puede ser un hacker)");
        }

        Appointment actuallyAppointment = appointmentRepository.findByIdAndBusinessId(appointmentIdFromPath, business.getId())
                .orElseThrow(() ->
                        new RuntimeException("No existe esta cita con AppointmentId %s y el Id de negocio %s, en base de datos".formatted(appointmentIdFromPath, business.getId())));

        BeanUtils.copyProperties(updatedAppointment, actuallyAppointment, "id", "business", "createdAt", "technicianId");


        if (updatedAppointment.getTechnicianId() != null) {
            Technician technician = technicianRepository.findById(updatedAppointment.getTechnicianId())
                    .orElseThrow(() -> new RuntimeException("Técnico no encontrado con el id: " + updatedAppointment.getTechnicianId()));
            actuallyAppointment.setTechnician(technician);
        }

        Appointment savedAppointment = appointmentRepository.save(actuallyAppointment);

        LOG.info("@updateAppointment SERV > The update for the appointment with the ID {} is complete; updated information saved.", savedAppointment.getId());

        return AppointmentMapper.fromEntity(savedAppointment);
    }
    @Transactional
    public void deleteAppointment (Long id, Long businessId){

        if(id == null || businessId == null) {
            throw new IllegalArgumentException("Uno de los identificadorees está nulo.");
        }

        Appointment appointment = appointmentRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow( ()->
                        new NoSuchElementException("No existe una cita con esos datos"));

        appointmentRepository.delete(appointment);

        appointmentRepository.flush();

        if (appointmentRepository.existsById(id)) {
            throw new IllegalStateException("No se pudo eliminar el registro correctamente");
        }
    }
}
