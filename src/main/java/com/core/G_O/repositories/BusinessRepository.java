package com.core.G_O.repositories;

import com.core.G_O.models.Business;
import com.core.G_O.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByUserOwner(User userId);

    Boolean existsByBusinessName(String name);
    Boolean existsByBusinessPhoneNumber(String phone);
}
