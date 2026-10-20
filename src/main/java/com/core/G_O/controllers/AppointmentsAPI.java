package com.core.G_O.controllers;

import com.core.G_O.DTO.AppointmentsDTOs.AppointmentRequestDTO;
import com.core.G_O.DTO.AppointmentsDTOs.AppointmentResponseDTO;
import com.core.G_O.DTO.AppointmentsDTOs.AppointmentUpdateDTO;
import com.core.G_O.models.Appointment;
import com.core.G_O.service.AppointmentService;
import com.core.G_O.service.JwtService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class AppointmentsAPI {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentsAPI.class);

    AppointmentService appointmentService;
    JwtService jwtService;

    public AppointmentsAPI(AppointmentService appointmentService, JwtService jwtService){
        this.appointmentService = appointmentService;
        this.jwtService = jwtService;
    }

    @GetMapping("/get")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentByBusinessID(@RequestHeader("Authorization") String header){

        LOG.info("@getAppointmentByBusinessID API > Start the process to obtain the appointments associated with the business_id");

        String token = header.substring(7);

        Long businessId = jwtService.getBusinessId(token);
        List<AppointmentResponseDTO> response = appointmentService.getAppointmentsByBusinessID(businessId);

        LOG.info("@getAppointmentByBusinessID API > Ends the process to obtain the appointments associated with the business_id: {}, the appointments " +
                "obtained were {}", businessId, response);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/create")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO appointment, @RequestHeader("Authorization") String header){

        LOG.info("@createAppointment API > Start the process to create an appointment: {}", appointment);

        String token = header.substring(7);

        Long businessId =jwtService.getBusinessId(token);
        AppointmentResponseDTO response = appointmentService.createAppointment(appointment, businessId);

        LOG.info("@createAppointment API > Ends the process to create an appointment, created: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    };

    @PutMapping("/update/{id}")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment (@PathVariable("id") Long appointmentId, @RequestBody AppointmentUpdateDTO appointmentDetails, @RequestHeader("Authorization") String header) {

        LOG.info("@updateAppointment API > Start api to updated the appointment with id {}", appointmentDetails.getId());

        String token = header.substring(7);

        AppointmentResponseDTO updatedAppointment = appointmentService.updateAppointment(appointmentId, appointmentDetails, token);

        LOG.info("@updateAppointment API > The update for the appointment with the ID {} is complete", updatedAppointment.getId());

        return ResponseEntity.status(HttpStatus.OK).body(updatedAppointment);
    };

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAppointment (@PathVariable("id") Long id, @RequestHeader("Authorization") String header) {

        LOG.info("@deleteAppointment API > Start api to delete appointment with id: [{}]", id);

        String token = header.substring(7);

        Long businessId = jwtService.getBusinessId(token);
        appointmentService.deleteAppointment(id, businessId);

        LOG.info("@deleteAppointment API > End api to delete appointment.");

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    };
}
