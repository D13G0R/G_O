package com.core.G_O.repositories;

import com.core.G_O.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByPhoneNumber(String phoneNumber);
    Boolean existsByEmail(String email);
}
