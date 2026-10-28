package com.core.G_O.repositories;

import com.core.G_O.DTO.DashboardDTOs.DashboardSummaryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Repository
public class DashboardRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public DashboardSummaryDTO getSummary(Long businessId,
                                          LocalDate today,
                                          OffsetDateTime dayStart,
                                          OffsetDateTime monthStart,
                                          OffsetDateTime now) {

        String sql = """
                WITH technician_metrics AS (
                    SELECT
                        COUNT(*) AS total_technicians,
                        COUNT(*) FILTER (WHERE u.active = true) AS active_technicians
                    FROM technicians t
                    LEFT JOIN users u ON u.id = t.user_id
                    WHERE t.work_at = :businessId
                ),
                revenue_metrics AS (
                    SELECT
                        COALESCE(SUM(amount) FILTER (
                            WHERE reviewed_at >= :dayStart AND reviewed_at <= :now
                        ), 0) AS today_revenue,
                        COALESCE(SUM(amount) FILTER (
                            WHERE reviewed_at >= :monthStart AND reviewed_at <= :now
                        ), 0) AS month_revenue
                    FROM payment_reviews
                    WHERE business_id = :businessId
                      AND status = 'APPROVED'
                      AND reviewed_at >= :monthStart
                      AND reviewed_at <= :now
                ),
                appointment_metrics AS (
                    SELECT
                        COUNT(*) FILTER (
                            WHERE lower(status) IN ('scheduled', 'pending')
                        ) AS today_scheduled_appointments,
                        COUNT(*) FILTER (
                            WHERE lower(status) = 'confirmed'
                        ) AS today_confirmed_appointments,
                        COUNT(*) FILTER (
                            WHERE lower(status) = 'in_progress'
                        ) AS today_in_progress_appointments,
                        COUNT(*) FILTER (
                            WHERE lower(status) = 'pending_payment'
                        ) AS today_pending_payment_appointments,
                        COUNT(*) FILTER (
                            WHERE lower(status) = 'payment_review'
                        ) AS today_payment_review_appointments,
                        COUNT(*) FILTER (
                            WHERE lower(status) = 'payment_rejected'
                        ) AS today_payment_rejected_appointments,
                        COUNT(*) FILTER (
                            WHERE lower(status) = 'completed'
                        ) AS today_completed_appointments,
                        COUNT(*) FILTER (
                            WHERE lower(status) = 'cancelled'
                        ) AS today_cancelled_appointments,
                        COUNT(*) FILTER (
                            WHERE status IS NULL
                               OR lower(status) NOT IN (
                                   'scheduled', 'pending', 'confirmed', 'in_progress',
                                   'pending_payment', 'payment_review', 'payment_rejected',
                                   'completed', 'cancelled'
                               )
                        ) AS today_other_appointments,
                        COUNT(*) AS total_today_appointments
                    FROM appointments
                    WHERE business_id = :businessId
                      AND appointment_date = :today
                )
                SELECT
                    tm.total_technicians,
                    tm.active_technicians,
                    rm.today_revenue,
                    rm.month_revenue,
                    am.today_scheduled_appointments,
                    am.today_confirmed_appointments,
                    am.today_in_progress_appointments,
                    am.today_pending_payment_appointments,
                    am.today_payment_review_appointments,
                    am.today_payment_rejected_appointments,
                    am.today_completed_appointments,
                    am.today_cancelled_appointments,
                    am.today_other_appointments,
                    am.total_today_appointments
                FROM technician_metrics tm
                CROSS JOIN revenue_metrics rm
                CROSS JOIN appointment_metrics am
                """;

        Object[] row = (Object[]) entityManager.createNativeQuery(sql)
                .setParameter("businessId", businessId)
                .setParameter("today", today)
                .setParameter("dayStart", dayStart)
                .setParameter("monthStart", monthStart)
                .setParameter("now", now)
                .getSingleResult();

        return new DashboardSummaryDTO(
                toLong(row[0]),
                toLong(row[1]),
                toBigDecimal(row[2]),
                toBigDecimal(row[3]),
                toLong(row[4]),
                toLong(row[5]),
                toLong(row[6]),
                toLong(row[7]),
                toLong(row[8]),
                toLong(row[9]),
                toLong(row[10]),
                toLong(row[11]),
                toLong(row[12]),
                toLong(row[13])
        );
    }

    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
    }
}
