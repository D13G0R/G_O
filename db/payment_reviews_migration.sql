-- Migración: validación de pagos y acceso de técnicos
-- Ejecutar sobre general_db

ALTER TABLE roles ADD CONSTRAINT roles_rol_name_key UNIQUE (rol_name);

-- La secuencia de roles puede estar desincronizada si los roles se insertaron manualmente.
INSERT INTO roles (id, rol_name) VALUES (2, 'technician') ON CONFLICT DO NOTHING;
SELECT setval('roles_id_seq', (SELECT max(id) FROM roles));

ALTER TABLE technicians ADD COLUMN user_id integer;
ALTER TABLE technicians ADD CONSTRAINT technicians_user_id_key UNIQUE (user_id);
ALTER TABLE technicians ADD CONSTRAINT technicians_user_id_fkey
  FOREIGN KEY (user_id) REFERENCES users(id);

UPDATE technicians t SET user_id = u.id
FROM users u WHERE u.email = t.email AND t.user_id IS NULL;

CREATE TABLE payment_reviews (
  id bigserial PRIMARY KEY,
  appointment_id integer NOT NULL UNIQUE REFERENCES appointments(id) ON DELETE CASCADE,
  business_id integer NOT NULL REFERENCES businesses(id),
  technician_id integer NOT NULL REFERENCES technicians(id),
  amount numeric(10,0) NOT NULL CHECK (amount >= 0),
  method varchar(40) NOT NULL,
  reference varchar(100),
  evidence_path varchar(500) NOT NULL,
  evidence_content_type varchar(100) NOT NULL,
  status varchar(20) NOT NULL DEFAULT 'PENDING',
  observation text,
  submitted_at timestamptz NOT NULL DEFAULT now(),
  reviewed_at timestamptz,
  reviewed_by integer REFERENCES users(id)
);

CREATE INDEX idx_payment_reviews_business_status
  ON payment_reviews(business_id, status);
