package com.core.G_O.controllers;

import com.core.G_O.models.Appointment;
import com.core.G_O.service.AppointmentService;
import com.core.G_O.service.JwtService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
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

    @GetMapping("/get/{business_id}")
    public ResponseEntity<List<Appointment>> getAppointmentByBusinessID(@PathVariable Long business_id){

        LOG.info("Start the process to obtain the appointments associated with the business_id: {}", business_id);

        List<Appointment> appointments = appointmentService.getAppointmentsByBusinessID(business_id);

        LOG.info("Ends the process to obtain the appointments associated with the business_id: {}, the appointments " +
                "obtained were {}", business_id, appointments);

        return ResponseEntity.status(HttpStatus.OK).body(appointments);
    }

    @PostMapping("/create")
    public ResponseEntity<Appointment> createAppointment(@Valid @RequestBody Appointment appointment){

        LOG.info("Start the process to create an appointment: {}", appointment);

        Appointment created = appointmentService.createAppointment(appointment);

        LOG.info("Ends the process to create an appointment, created: {}", created);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);

    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Appointment> updateAppointment (@PathVariable Long id, @RequestBody Appointment appointmentDetails) {

        LOG.info("Start api to updated the appointment with id {}", appointmentDetails.getId());

        Appointment updatedAppointment = appointmentService.updateAppointment(id, appointmentDetails);

        LOG.info("The update for the appointment with the ID {} is complete", updatedAppointment.getId());

        return ResponseEntity.status(HttpStatus.OK).body(updatedAppointment);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAppointment (@PathVariable Long id, @RequestHeader("Authorization") String header) {

        LOG.info("@deleteAppointment API > Start api to delete appointment with id: [{}]", id);

        String token = header.substring(7);

        Long businessId = jwtService.getBusinessId(token);
        appointmentService.deleteAppointment(id, businessId);

        LOG.info("@deleteAppointment API > End api to delete appointment.");

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
