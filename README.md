# AtencIA Backend — G_O

API REST multi-negocio para la gestión de citas, técnicos, configuración del negocio y validación de pagos con comprobantes. El backend expone autenticación stateless con JWT, aislamiento de datos por negocio, almacenamiento de evidencias en disco y notificaciones en tiempo real por WebSocket.

## Tecnologías

| Componente | Versión / tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.16 |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL |
| Seguridad | Spring Security + JWT (JJWT 0.12.5) |
| Validación | Jakarta Bean Validation |
| Tiempo real | Spring WebSocket |
| Documentación API | Springdoc OpenAPI 2.8.16 |
| Utilidades | Lombok |
| Build | Maven Wrapper (`mvnw`, `mvnw.cmd`) |
| Contenedores | Docker + Docker Compose |

## Capacidades principales

- Registro de negocios y usuarios propietarios.
- Autenticación con JWT y contraseñas cifradas con BCrypt.
- Gestión de citas por negocio y por técnico.
- Registro y vinculación de técnicos con acceso propio a la aplicación.
- Reporte de pagos por parte del técnico con comprobante adjunto.
- Aprobación o rechazo de pagos por parte del propietario.
- Descarga autenticada de evidencias.
- Notificaciones en tiempo real para pagos enviados y revisados.
- Configuración del contexto del negocio usada por automatizaciones n8n.
- CORS restringido por lista explícita de orígenes.

## Arquitectura

El proyecto sigue una separación por capas:

```text
src/main/java/com/core/G_O
├── controllers     Endpoints REST
├── DTO             Contratos de entrada y salida
├── Exception       Manejo centralizado de errores
├── models          Entidades JPA
├── repositories    Repositorios Spring Data
├── security        Filtro JWT y configuración de seguridad
├── service         Lógica de negocio
├── utils           Mappers y utilidades
└── websocket       Configuración, handshake y handler de pagos
```

La API es stateless: no se crean sesiones HTTP. El `business_id` y el rol se obtienen del JWT, no de parámetros enviados por el cliente. Las operaciones sobre citas, técnicos y pagos validan que los recursos pertenezcan al negocio autenticado.

## Entidades principales

| Entidad | Propósito | Relaciones clave |
|---|---|---|
| `User` | Usuario autenticable del sistema. | `N:1` con `Role`; puede ser propietario o técnico. |
| `Business` | Negocio o tenant lógico. | `N:1` con `User` propietario. |
| `Technician` | Ficha operativa del técnico. | `N:1` opcional con `User`; `N:1` con `Business` (`work_at`); `N:1` con `User` jefe (`boss`). |
| `Appointment` | Cita o servicio agendado. | `N:1` con `Business`; `N:1` opcional con `Technician`. |
| `PaymentReview` | Reporte y revisión del pago de una cita. | `1:1` con `Appointment`; `N:1` con `Business`, `Technician` y `User` revisor. |
| `Role` | Rol del usuario (`owner`, `technician`). | Referenciado por `User`. |

Reglas relevantes:

- Un técnico puede existir sin cuenta de usuario y vincularse posteriormente.
- Una cita solo puede tener una revisión de pago (`appointment_id` único).
- Un reenvío de pago actualiza la misma revisión; no se conserva historial de intentos.
- El comprobante se almacena en disco y en la base se guardan la ruta relativa y el tipo de contenido.

## Rutas principales

| Método | Ruta | Acceso |
|---|---|---|
| `POST` | `/api/auth/login` | Público |
| `POST` | `/api/auth/register` | Público |
| `GET` | `/appointments/get` | Propietario autenticado |
| `GET` | `/appointments/my` | Técnico autenticado; propietario en modo lectura |
| `GET` | `/appointments/get/technician/{id}` | Propietario o técnico de la misma cuenta |
| `POST` | `/appointments/create` | Propietario autenticado |
| `PUT` | `/appointments/update/{id}` | Propietario autenticado |
| `DELETE` | `/appointments/delete/{id}` | Propietario autenticado |
| `POST` | `/appointments/{id}/payment` | Técnico asignado; `multipart/form-data` |
| `GET` | `/payments?status=ALL\|PENDING\|APPROVED\|REJECTED` | Propietario autenticado |
| `PATCH` | `/payments/{id}/review` | Propietario autenticado |
| `GET` | `/payments/{id}/evidence` | Propietario o técnico del pago |
| `GET` | `/technicians/get` | Propietario autenticado |
| `POST` | `/technicians/register` | Propietario autenticado |
| `GET` | `/business-configuration` | Usuario autenticado |
| `PATCH` | `/business-configuration` | Usuario autenticado |
| `WS` | `/ws/payments?token=<JWT>` | Handshake autenticado |

La documentación interactiva de OpenAPI está disponible en:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

## Requisitos previos

- JDK 21.
- PostgreSQL accesible.
- Docker y Docker Compose (opcional, para el entorno contenedorizado).
- Variables de entorno configuradas.

## Variables de entorno

Copia `.env.example` como `.env` y completa los valores. El archivo `.env` está ignorado por Git.

| Variable | Descripción | Obligatoria |
|---|---|---|
| `DB_PASSWORD` | Contraseña de PostgreSQL para ejecución local con `spring-boot:run`. | Sí, en local |
| `DOCKER_DB_PASSWORD` | Contraseña usada por PostgreSQL y la API en Docker Compose. Debe coincidir con el volumen existente. | Sí, en Docker |
| `JWT_SECRET` | Secreto de firma JWT. Mínimo 32 bytes; puede ser texto plano o Base64. | Sí en producción |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma. No se admite `*`. | Recomendada |
| `EVIDENCE_DIR` | Directorio de comprobantes. Por defecto `./uploads/evidence`. | No |

> Si `JWT_SECRET` no se configura, la aplicación genera una clave efímera y los tokens se invalidan al reiniciar. No uses ese comportamiento en producción.

## Ejecución local

### 1. Base de datos

La configuración local apunta a:

```text
jdbc:postgresql://127.0.0.1:5434/general_db
```

Si utilizas otra instancia o puerto, define `SPRING_DATASOURCE_URL` al ejecutar la aplicación.

### 2. Esquema

Hibernate está configurado con `ddl-auto=none` para no modificar la base compartida con n8n. Aplica las migraciones SQL versionadas antes de iniciar:

```bash
psql -U postgres -d general_db -f db/payment_reviews_migration.sql
```

Revisa el contenido de `db/` para otras migraciones disponibles.

### 3. Aplicación

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

### 4. Compilación y pruebas

```bash
# Linux / macOS
./mvnw clean package
./mvnw test

# Windows
.\mvnw.cmd clean package
.\mvnw.cmd test
```

## Ejecución con Docker

El servicio `server` está bajo el perfil `app`, por lo que no se inicia con el comando por defecto.

```bash
# Solo PostgreSQL
docker compose up -d db

# PostgreSQL + API
docker compose --profile app up --build
```

Servicios y puertos:

| Servicio | Contenedor | Puerto host |
|---|---|---|
| API | `GO-API` | `8080` |
| PostgreSQL | `GO-DB` | `5433` |

Notas operativas:

- `DOCKER_DB_PASSWORD` debe coincidir con la contraseña del volumen existente. Cambiar la variable no cambia la contraseña de un volumen ya inicializado.
- No ejecutes `docker compose down -v` si la base se comparte con n8n u otro sistema.
- Los comprobantes se guardan en el volumen `evidence-data` y se montan en `/data/evidence`.
- El contenedor se ejecuta con el usuario no privilegiado `appuser`.
- `db/password.txt` fue retirado del repositorio. No vuelvas a versionar secretos.

## Seguridad

- Contraseñas almacenadas con BCrypt.
- JWT firmado con `JWT_SECRET`, expiración de 1 hora.
- Claims utilizados: `user_id`, `user_name`, `email`, `business_id`, `role`, `technician_id`.
- `JwtAuthenticationFilter` valida firma, expiración y estado activo del usuario.
- CORS con allowlist explícita; `*` se rechaza.
- El handshake WebSocket valida el JWT y aplica la misma allowlist de orígenes.
- El acceso a citas, técnicos y pagos se filtra por `business_id`.
- Las evidencias no se sirven como archivos estáticos; se descargan mediante un endpoint autenticado.

Limitaciones conocidas: no hay refresh token ni revocación de JWT, y el token se envía en la query string durante el handshake WebSocket. Revisa `../PENDIENTES.md` para el plan de endurecimiento.

## WebSocket de pagos

- Endpoint: `ws://localhost:8080/ws/payments?token=<JWT>`.
- Eventos emitidos: `PAYMENT_SUBMITTED` y `PAYMENT_REVIEWED`.
- El payload contiene `type` y el objeto `payment`.
- El envío se realiza después del commit de la transacción.
- Si el destinatario no tiene una sesión abierta, el evento no se encola.

## Evidencias

- Tamaño máximo por archivo: 10 MB.
- Tamaño máximo de request: 12 MB.
- Tipos permitidos: `image/jpeg`, `image/png`, `image/webp`, `application/pdf`.
- Los archivos se guardan con nombre UUID dentro del directorio configurado.
- La base de datos conserva únicamente la ruta relativa y el tipo de contenido.
- La descarga exige autenticación y autorización sobre el pago.

## Documentación interna

- [Contexto del proyecto](../PROJECT_CONTEXT.md)
- [Contexto del frontend](../FRONTEND_CONTEXT.md)
- [Backlog y deuda técnica](../PENDIENTES.md)
- [Historial de cambios](../CHANGELOG_FEATURES.md)
