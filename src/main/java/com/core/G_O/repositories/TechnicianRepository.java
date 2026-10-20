package com.core.G_O.repositories;

import com.core.G_O.models.Technician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {

    Optional<List<Technician>> findByWorkAt_Id(Long businessId);
}
