-- Índices para las métricas del dashboard
-- Ejecutar sobre general_db

CREATE INDEX IF NOT EXISTS idx_appointments_business_date_status_lower
  ON appointments (business_id, appointment_date, lower(status));

CREATE INDEX IF NOT EXISTS idx_payment_reviews_approved_business_reviewed_at
  ON payment_reviews (business_id, reviewed_at)
  WHERE status = 'APPROVED';

CREATE INDEX IF NOT EXISTS idx_technicians_work_at
  ON technicians (work_at);
