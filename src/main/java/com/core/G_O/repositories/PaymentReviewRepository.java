package com.core.G_O.repositories;

import com.core.G_O.models.PaymentReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentReviewRepository extends JpaRepository<PaymentReview, Long> {

    List<PaymentReview> findByBusiness_IdOrderBySubmittedAtDesc(Long businessId);

    List<PaymentReview> findByBusiness_IdAndStatusOrderBySubmittedAtDesc(Long businessId, String status);

    Optional<PaymentReview> findByIdAndBusiness_Id(Long id, Long businessId);

    Optional<PaymentReview> findByAppointment_Id(Long appointmentId);

    List<PaymentReview> findByAppointment_Business_Id(Long businessId);
}
