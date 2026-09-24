package com.core.G_O.service;

import com.core.G_O.models.Appointment;
import com.core.G_O.repositories.AppointmentRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AppointmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepositories appointmentRepositories;

    public AppointmentService(AppointmentRepositories appointmentRepositories){
        this.appointmentRepositories = appointmentRepositories;
    }

    public List<Appointment> getAppointmentsByBusinessID(Long business_id){

        LOG.info("@getAppointmentsByBusinessID SERV > Stars the service to obtain all the apointments of the business_id");

        List<Appointment> appointments =  appointmentRepositories.findByBusinessId(business_id);

        LOG.info("@getAppointmentsByBusinessID SERV > Ends the service, The appointments obtained were {} for the business_id: {}.",
                appointments, business_id);

        return appointments;
    }

    public Appointment createAppointment(Appointment appointment){
        LOG.info("@createAppointment SERV > Starts the service to save the appointment");
        Appointment saved = appointmentRepositories.save(appointment);
        LOG.info("@createAppointment SERV > Ends the service, saved appointment: {}", saved);
        return saved;
    }
}
