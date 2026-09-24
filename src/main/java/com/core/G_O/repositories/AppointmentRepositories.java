package com.core.G_O.repositories;
import java.util.List;

import com.core.G_O.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepositories extends JpaRepository<Appointment, Long> {

    List<Appointment> findByBusinessId(Long businessId);

    
}
