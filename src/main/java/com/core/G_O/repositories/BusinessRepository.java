package com.core.G_O.repositories;

import com.core.G_O.models.BusinessContext;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<BusinessContext, Long> {

    Optional<BusinessContext> findByPhoneNumberId(String phoneNumberId);

    Boolean existsByPhoneNumberId(String phoneNumberId);
}
