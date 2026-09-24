package com.core.G_O.controllers;

import com.core.G_O.models.Appointment;
import com.core.G_O.service.AppointmentService;
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

    public AppointmentsAPI(AppointmentService appointmentService){
        this.appointmentService = appointmentService;
    }

    @GetMapping("/{business_id}")
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
}
