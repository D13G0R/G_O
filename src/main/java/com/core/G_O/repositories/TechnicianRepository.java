package com.core.G_O.repositories;

import com.core.G_O.models.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TechnicianRepository extends JpaRepository<Technician, Long> {
    List<Technician> findByBusinessId(Long businessId);
}
