package com.core.G_O.controllers;

import com.core.G_O.DTO.AppointmentsDTOs.AppointmentRequestDTO;
import com.core.G_O.DTO.AppointmentsDTOs.AppointmentResponseDTO;
import com.core.G_O.DTO.AppointmentsDTOs.AppointmentUpdateDTO;
import com.core.G_O.DTO.AppointmentsDTOs.StopwatchUpdateDTO;
import com.core.G_O.DTO.PaymentsDTOs.PaymentReviewResponseDTO;
import com.core.G_O.Exception.AtencIAException;
import com.core.G_O.models.User;
import com.core.G_O.service.AppointmentService;
import com.core.G_O.service.BusinessContextN8nService;
import com.core.G_O.service.JwtService;
import com.core.G_O.service.PaymentService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/appointments")
public class AppointmentsAPI {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentsAPI.class);

    AppointmentService appointmentService;
    JwtService jwtService;
    BusinessContextN8nService businessContextN8nService;
    PaymentService paymentService;

    public AppointmentsAPI(AppointmentService appointmentService,
                           JwtService jwtService,
                           BusinessContextN8nService businessContextN8nService,
                           PaymentService paymentService){
        this.appointmentService = appointmentService;
        this.jwtService = jwtService;
        this.businessContextN8nService = businessContextN8nService;
        this.paymentService = paymentService;
    }

    @GetMapping("/get")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentByBusinessID(@RequestHeader("Authorization") String header){

        LOG.info("@getAppointmentByBusinessID API > Start the process to obtain the appointments associated to business_id");

        String token = header.substring(7);

        Long businessId = jwtService.getBusinessId(token);
        List<AppointmentResponseDTO> response = appointmentService.getAppointments(businessId, null);

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

    @GetMapping("/get/technician/{id}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsAssociatedTechnicians(
            @PathVariable(value = "id") Long technicianId,
            @AuthenticationPrincipal User user,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorizationHeader) {

        LOG.info("@getAppointmentsAssociatedTechnicians API > Call the API to retrieve all appointments scheduled for " +
                "today for the technician with ID {}", technicianId);

        String token = authorizationHeader.substring(7);
        Long businessId = jwtService.getBusinessId(token);
        String role = jwtService.getRole(token);

        if (!"owner".equalsIgnoreCase(role)
                && !Objects.equals(jwtService.getTechnicianId(token), technicianId)) {
            throw new AtencIAException(HttpStatus.FORBIDDEN,
                    "No puedes consultar las citas de otro técnico");
        }

        List<AppointmentResponseDTO> appointments = appointmentService.getAppointments(businessId, technicianId);

        LOG.info("@getAppointmentsAssociatedTechnicians API > Call the API to retrieve all appointments scheduled and confirmed for " +
                "today for the technician with ID {} has ended, the retrieved appointments were {}", technicianId, appointments);

        return ResponseEntity.status(HttpStatus.OK).body(appointments);
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponseDTO>> getMyAppointments(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorizationHeader) {

        String token = authorizationHeader.substring(7);
        Long businessId = jwtService.getBusinessId(token);
        Long technicianId = jwtService.getTechnicianId(token);

        LOG.info("@getMyAppointments API > Getting appointments for technician {}", technicianId);

        if (technicianId == null && "owner".equalsIgnoreCase(jwtService.getRole(token))) {
            return ResponseEntity.ok(appointmentService.getAllAppointments(businessId));
        }

        return ResponseEntity.ok(appointmentService.getMyAppointments(businessId, technicianId));
    }

    @PatchMapping("/{id}/stopwatch")
    public ResponseEntity<AppointmentResponseDTO> updateStopwatch(
            @PathVariable("id") Long appointmentId,
            @Valid @RequestBody StopwatchUpdateDTO request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorizationHeader) {

        LOG.info("@updateStopwatch API > Updating stopwatch for appointment {} with action {}",
                appointmentId, request.getAction());

        String token = authorizationHeader.substring(7);
        AppointmentResponseDTO response = appointmentService.updateStopwatch(
                appointmentId, request.getAction(), token);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/payment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PaymentReviewResponseDTO> submitPayment(
            @PathVariable("id") Long appointmentId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("method") String method,
            @RequestParam(value = "reference", required = false) String reference,
            @RequestPart("evidence") MultipartFile evidence,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authorizationHeader) {

        String token = authorizationHeader.substring(7);
        Long businessId = jwtService.getBusinessId(token);
        Long technicianId = jwtService.getTechnicianId(token);

        LOG.info("@submitPayment API > Technician {} reporting payment for appointment {}", technicianId, appointmentId);

        PaymentReviewResponseDTO response = paymentService.submitPayment(
                appointmentId, businessId, technicianId, amount, method, reference, evidence);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
