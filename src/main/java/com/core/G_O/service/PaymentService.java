package com.core.G_O.service;

import com.core.G_O.DTO.PaymentsDTOs.PaymentEventDTO;
import com.core.G_O.DTO.PaymentsDTOs.PaymentReviewResponseDTO;
import com.core.G_O.Exception.AtencIAException;
import com.core.G_O.models.Appointment;
import com.core.G_O.models.PaymentReview;
import com.core.G_O.models.Technician;
import com.core.G_O.models.User;
import com.core.G_O.repositories.AppointmentRepository;
import com.core.G_O.repositories.PaymentReviewRepository;
import com.core.G_O.service.EvidenceStorageService.StoredEvidence;
import com.core.G_O.websocket.PaymentWebSocketHandler;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class PaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentService.class);

    private static final Set<String> REVIEW_STATUSES = Set.of(
            PaymentReview.APPROVED,
            PaymentReview.REJECTED
    );

    private final AppointmentRepository appointmentRepository;
    private final PaymentReviewRepository paymentReviewRepository;
    private final EvidenceStorageService evidenceStorageService;
    private final PaymentWebSocketHandler paymentWebSocketHandler;

    public PaymentService(AppointmentRepository appointmentRepository,
                          PaymentReviewRepository paymentReviewRepository,
                          EvidenceStorageService evidenceStorageService,
                          PaymentWebSocketHandler paymentWebSocketHandler) {
        this.appointmentRepository = appointmentRepository;
        this.paymentReviewRepository = paymentReviewRepository;
        this.evidenceStorageService = evidenceStorageService;
        this.paymentWebSocketHandler = paymentWebSocketHandler;
    }

    @Transactional
    public PaymentReviewResponseDTO submitPayment(Long appointmentId,
                                                  Long businessId,
                                                  Long technicianId,
                                                  BigDecimal amount,
                                                  String method,
                                                  String reference,
                                                  MultipartFile evidence) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST, "El monto cobrado debe ser mayor que cero");
        }

        if (method == null || method.isBlank() || method.length() > 40) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST, "El método de pago es obligatorio");
        }

        if (reference != null && reference.length() > 100) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST, "La referencia no puede superar 100 caracteres");
        }

        Appointment appointment = appointmentRepository.findByIdAndBusinessId(appointmentId, businessId)
                .orElseThrow(() -> new AtencIAException(HttpStatus.NOT_FOUND, "No existe la cita indicada"));

        Technician technician = appointment.getTechnician();

        if (technician == null || !technician.getId().equals(technicianId)) {
            throw new AtencIAException(HttpStatus.FORBIDDEN, "La cita no está asignada al técnico autenticado");
        }

        if ("cancelled".equalsIgnoreCase(appointment.getStatus())) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST, "No se puede reportar el pago de una cita cancelada");
        }

        PaymentReview review = paymentReviewRepository.findByAppointment_Id(appointmentId).orElse(null);

        if (review != null && PaymentReview.APPROVED.equals(review.getStatus())) {
            throw new AtencIAException(HttpStatus.CONFLICT, "El pago de esta cita ya fue aprobado");
        }

        StoredEvidence storedEvidence = evidenceStorageService.save(evidence, businessId);
        String previousEvidence = review == null ? null : review.getEvidencePath();

        try {
            if (review == null) {
                review = PaymentReview.builder()
                        .appointment(appointment)
                        .business(appointment.getBusiness())
                        .technician(technician)
                        .submittedAt(LocalDateTime.now())
                        .status(PaymentReview.PENDING)
                        .build();
            }

            review.setAmount(amount);
            review.setMethod(method.trim());
            review.setReference(reference == null || reference.isBlank() ? null : reference.trim());
            review.setEvidencePath(storedEvidence.relativePath());
            review.setEvidenceContentType(storedEvidence.contentType());
            review.setStatus(PaymentReview.PENDING);
            review.setObservation(null);
            review.setReviewedAt(null);
            review.setReviewedBy(null);
            review.setSubmittedAt(LocalDateTime.now());

            appointment.setStatus("payment_review");
            appointment.setPaid(amount);
            appointmentRepository.save(appointment);

            PaymentReview saved = paymentReviewRepository.save(review);

            if (previousEvidence != null && !previousEvidence.equals(storedEvidence.relativePath())) {
                evidenceStorageService.delete(previousEvidence);
            }

            PaymentReviewResponseDTO response = toResponse(saved);

            notifyOwner(saved, response, "PAYMENT_SUBMITTED");

            LOG.info("@PaymentService > Payment {} reported for appointment {} by technician {}",
                    saved.getId(), appointmentId, technicianId);

            return response;
        } catch (RuntimeException exception) {
            evidenceStorageService.delete(storedEvidence.relativePath());
            throw exception;
        }
    }

    @Transactional
    public List<PaymentReviewResponseDTO> getPayments(Long businessId, String status) {
        List<PaymentReview> reviews;

        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            reviews = paymentReviewRepository.findByBusiness_IdOrderBySubmittedAtDesc(businessId);
        } else {
            reviews = paymentReviewRepository.findByBusiness_IdAndStatusOrderBySubmittedAtDesc(
                    businessId, status.toUpperCase(Locale.ROOT));
        }

        return reviews.stream().map(this::toResponse).toList();
    }

    @Transactional
    public PaymentReviewResponseDTO reviewPayment(Long paymentId,
                                                  Long businessId,
                                                  User reviewer,
                                                  String status,
                                                  String observation) {

        String normalizedStatus = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);

        if (!REVIEW_STATUSES.contains(normalizedStatus)) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST, "El estado debe ser APPROVED o REJECTED");
        }

        if (PaymentReview.REJECTED.equals(normalizedStatus)
                && (observation == null || observation.isBlank())) {
            throw new AtencIAException(HttpStatus.BAD_REQUEST,
                    "Debes agregar una observación para rechazar el pago");
        }

        PaymentReview review = paymentReviewRepository.findByIdAndBusiness_Id(paymentId, businessId)
                .orElseThrow(() -> new AtencIAException(HttpStatus.NOT_FOUND, "No existe el pago indicado"));

        if (!PaymentReview.PENDING.equals(review.getStatus())) {
            throw new AtencIAException(HttpStatus.CONFLICT, "Este pago ya fue revisado");
        }

        review.setStatus(normalizedStatus);
        review.setObservation(observation == null || observation.isBlank() ? null : observation.trim());
        review.setReviewedAt(LocalDateTime.now());
        review.setReviewedBy(reviewer);

        Appointment appointment = review.getAppointment();
        appointment.setStatus(PaymentReview.APPROVED.equals(normalizedStatus)
                ? "completed"
                : "payment_rejected");
        appointmentRepository.save(appointment);

        PaymentReview saved = paymentReviewRepository.save(review);
        PaymentReviewResponseDTO response = toResponse(saved);

        notifyTechnician(saved, response, "PAYMENT_REVIEWED");

        LOG.info("@PaymentService > Payment {} reviewed as {} by user {}",
                paymentId, normalizedStatus, reviewer == null ? null : reviewer.getId());

        return response;
    }

    @Transactional
    public EvidenceResponse getEvidence(Long paymentId, Long businessId, Long technicianId, boolean owner) {
        PaymentReview review = paymentReviewRepository.findByIdAndBusiness_Id(paymentId, businessId)
                .orElseThrow(() -> new AtencIAException(HttpStatus.NOT_FOUND, "No existe el pago indicado"));

        if (!owner && !review.getTechnician().getId().equals(technicianId)) {
            throw new AtencIAException(HttpStatus.FORBIDDEN, "No puedes ver esta evidencia");
        }

        Resource resource = evidenceStorageService.load(review.getEvidencePath());

        return new EvidenceResponse(resource, review.getEvidenceContentType());
    }

    private PaymentReviewResponseDTO toResponse(PaymentReview review) {
        Appointment appointment = review.getAppointment();
        User reviewedBy = review.getReviewedBy();

        return PaymentReviewResponseDTO.builder()
                .id(review.getId())
                .appointmentId(appointment.getId())
                .businessId(review.getBusiness().getId())
                .technicianId(review.getTechnician().getId())
                .technicianName(review.getTechnician().getFullName())
                .technicianPhone(review.getTechnician().getPhoneNumber())
                .customerName(appointment.getCustomerName())
                .customerPhone(appointment.getCustomerPhone())
                .address(appointment.getAddress())
                .service(appointment.getApplianceType())
                .problemDescription(appointment.getProblemDescription())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .amount(review.getAmount())
                .method(review.getMethod())
                .reference(review.getReference())
                .status(review.getStatus())
                .observation(review.getObservation())
                .submittedAt(review.getSubmittedAt())
                .reviewedAt(review.getReviewedAt())
                .reviewedByName(reviewedBy == null ? null : reviewedBy.getFullName())
                .evidenceAvailable(true)
                .evidenceContentType(review.getEvidenceContentType())
                .evidenceUrl("/payments/" + review.getId() + "/evidence")
                .build();
    }

    private void notifyOwner(PaymentReview review,
                             PaymentReviewResponseDTO payment,
                             String type) {
        Long ownerId = review.getBusiness().getUserOwner() == null
                ? null
                : review.getBusiness().getUserOwner().getId();

        sendAfterCommit(ownerId, PaymentEventDTO.builder().type(type).payment(payment).build());
    }

    private void notifyTechnician(PaymentReview review,
                                  PaymentReviewResponseDTO payment,
                                  String type) {
        Long technicianUserId = review.getTechnician().getUser() == null
                ? null
                : review.getTechnician().getUser().getId();

        sendAfterCommit(technicianUserId, PaymentEventDTO.builder().type(type).payment(payment).build());
    }

    private void sendAfterCommit(Long userId, PaymentEventDTO event) {
        if (userId == null) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            paymentWebSocketHandler.sendToUser(userId, event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                paymentWebSocketHandler.sendToUser(userId, event);
            }
        });
    }

    public record EvidenceResponse(Resource resource, String contentType) {
    }
}
