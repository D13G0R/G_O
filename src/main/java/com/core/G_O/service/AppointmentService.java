package com.core.G_O.service;

import com.core.G_O.DTO.AppointmentRequestDTO;
import com.core.G_O.models.Appointment;
import com.core.G_O.models.Business;
import com.core.G_O.models.Technician;
import com.core.G_O.repositories.AppointmentRepository;
import com.core.G_O.repositories.TechnicianRepository;
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

    public AppointmentService(AppointmentRepository appointmentRepository, TechnicianRepository technicianRepository){
        this.appointmentRepository = appointmentRepository;
        this.technicianRepository = technicianRepository;
    }

    public List<Appointment> getAppointmentsByBusinessID(Long business_id){

        LOG.info("@getAppointmentsByBusinessID SERV > Stars the service to obtain all the apointments of the business_id");

        List<Appointment> appointments =  appointmentRepository.findByBusinessId(business_id);

        LOG.info("@getAppointmentsByBusinessID SERV > Ends the service, The appointments obtained were {} for the business_id: {}.",
                appointments, business_id);

        return appointments;
    }

    public Appointment createAppointment(AppointmentRequestDTO appointmentRequest){
        LOG.info("@createAppointment SERV > Starts the service to save the appointment");

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
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        LOG.info("@createAppointment SERV > Ends the service, saved appointment: {}", saved);
        return saved;
    }

    public Appointment updateAppointment (Long id, Appointment updatedAppointment ) {

        LOG.info("@updateAppointment SERV > Start update of the appointment with AppointmentId {}", updatedAppointment.getId());

        Long appointmentId = updatedAppointment.getId();
        Business business = updatedAppointment.getBusiness();

        if(id == null || appointmentId == null){
            throw new IllegalArgumentException("Uno de los indentificadores es null");
        }

        if(!Objects.equals(id, appointmentId)){
            throw new RuntimeException("Los ids no coinciden (alerta, puede ser un hacker)");
        }

        Appointment actuallyAppointent = appointmentRepository.findByIdAndBusinessId(appointmentId, business.getId())
                .orElseThrow(() ->
                        new RuntimeException("No existe esta cita con AppointmentId %s y el Id de negocio %s, en bases de datos".formatted(appointmentId, business.getId())));


        BeanUtils.copyProperties(updatedAppointment, actuallyAppointent, "id", "businessId", "createdAt");

        LOG.info("@updateAppointment SERV > The update for the appointment with the ID {} is complete; the appointment has been updated with this information: {}",actuallyAppointent.getId(), actuallyAppointent);

        return appointmentRepository.save(actuallyAppointent);



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
