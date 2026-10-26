-- Migración: cronómetro persistente por cita
-- Ejecutar sobre general_db

ALTER TABLE appointments
  ADD COLUMN IF NOT EXISTS start_time TIMESTAMPTZ NULL,
  ADD COLUMN IF NOT EXISTS end_time TIMESTAMPTZ NULL;
