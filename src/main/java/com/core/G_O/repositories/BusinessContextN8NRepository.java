package com.core.G_O.repositories;

import com.core.G_O.models.BusinessContextN8N;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BusinessContextN8NRepository extends JpaRepository<BusinessContextN8N, Long> {
    Optional<BusinessContextN8N> findByBusiness_Id(Long businessId);
}
