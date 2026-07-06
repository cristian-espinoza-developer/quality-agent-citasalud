# Data Model: Reserva de Cita en Línea 24/7

**Feature**: US-01 · 001-reserva-cita-online
**Date**: 2026-06-27

---

## Domain Entities (Capa Domain — innermost)

### Cita (Appointment)

Representa una reserva confirmada. Es el agregado raíz del proceso de reserva.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | `UUID` | NOT NULL, PK | Identificador único |
| `pacienteId` | `UUID` | NOT NULL | Referencia al paciente (sin FK entre bounded contexts) |
| `franjaHoraria` | `FranjaHoraria` | NOT NULL | Franja horaria asignada |
| `estado` | `EstadoCita` | NOT NULL | Estado actual de la cita |
| `fechaCreacion` | `LocalDateTime` | NOT NULL | Momento de la confirmación |

**Estado (EstadoCita)**:
```
CONFIRMADA → CANCELADA
CONFIRMADA → COMPLETADA
```

**Reglas de negocio**:
- Una `Cita` solo puede crearse si `FranjaHoraria.estado == DISPONIBLE`.
- El cambio de estado de `FranjaHoraria` a `OCUPADA` es atómico con la creación de `Cita`.
- Un paciente no puede tener dos citas `CONFIRMADA` con el mismo médico en la misma fecha
  (validación a nivel de Use Case).

---

### FranjaHoraria (Time Slot)

Representa un bloque de tiempo disponible en la agenda de un médico.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | `UUID` | NOT NULL, PK | Identificador único |
| `medicoId` | `UUID` | NOT NULL, IDX | Médico al que pertenece la franja |
| `fecha` | `LocalDate` | NOT NULL | Fecha de la franja |
| `horaInicio` | `LocalTime` | NOT NULL | Hora de inicio |
| `horaFin` | `LocalTime` | NOT NULL | Hora de fin |
| `estado` | `EstadoFranja` | NOT NULL | DISPONIBLE / OCUPADA |

**Estado (EstadoFranja)**:
```
DISPONIBLE → OCUPADA  (al confirmar reserva)
```

**Restricciones**:
- Índice único en `(medicoId, fecha, horaInicio)`: no puede haber dos franjas del mismo médico
  con el mismo inicio.
- `horaFin` MUST be strictly after `horaInicio`.
- Solo se leen/modifican franjas en esta historia; la creación de franjas la realiza un proceso
  administrativo separado.

**Concurrencia**: Al reservar, se aplica `PESSIMISTIC_WRITE` lock sobre la `FranjaHoraria`
antes de verificar y cambiar estado. Garantiza serialización de reservas concurrentes.

---

### Medico (Doctor)

Leído pero no modificado en este story.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | `UUID` | NOT NULL, PK | Identificador único |
| `nombre` | `String` | NOT NULL | Primer nombre |
| `apellido` | `String` | NOT NULL | Apellido(s) |
| `especialidad` | `String` | NOT NULL | Especialidad médica |
| `disponibleOnline` | `boolean` | NOT NULL, DEFAULT false | Habilitado para reserva online |

**Regla**: Solo los médicos con `disponibleOnline == true` aparecen en el listado de reserva online.

---

### Paciente (Patient)

Leído pero no modificado en este story. El `pacienteId` proviene del token de autenticación.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | `UUID` | NOT NULL, PK | Identificador único |
| `nombre` | `String` | NOT NULL | Primer nombre |
| `apellido` | `String` | NOT NULL | Apellido(s) |
| `telefonoWhatsApp` | `String` | NOT NULL | Número E.164 para notificación |

---

### NotificacionConfirmacion (Value Object — no persistida)

Objeto de valor que transporta los datos necesarios para el mensaje de WhatsApp.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `citaId` | `UUID` | ID de la cita confirmada |
| `pacienteNombreCompleto` | `String` | Nombre para el mensaje |
| `telefonoWhatsApp` | `String` | Destino de la notificación |
| `medicoNombreCompleto` | `String` | Nombre del médico |
| `medicoEspecialidad` | `String` | Especialidad |
| `fecha` | `LocalDate` | Fecha de la cita |
| `horaInicio` | `LocalTime` | Hora de inicio |
| `horaFin` | `LocalTime` | Hora de fin |

---

## Relationships

```
Paciente (1) ──────── (N) Cita
Medico   (1) ──────── (N) FranjaHoraria
                              │
FranjaHoraria (1) ─── (0..1) Cita
```

- Una `FranjaHoraria` puede tener cero o una `Cita` activa (CONFIRMADA).
- Al cancelar una `Cita`, la `FranjaHoraria` vuelve al estado `DISPONIBLE`
  (flujo de cancelación fuera del alcance de US-01).

---

## JPA Persistence Entities (Capa Interface Adapters — out/persistence)

Las entidades JPA son distintas de los domain models. El adapter de persistencia mapea entre ellas.

```
CitaJpaEntity          ↔  Cita
FranjaHorariaJpaEntity ↔  FranjaHoraria
MedicoJpaEntity        ↔  Medico
PacienteJpaEntity      ↔  Paciente
```

**Naming convention**: Sufijo `JpaEntity` para distinguirlas de los domain objects.

---

## State Transitions Summary

```
FranjaHoraria:  DISPONIBLE ──[reservar]──→ OCUPADA
Cita:           (nueva)    ──[confirmar]─→ CONFIRMADA
```

---

## Validation Rules (aplicadas en Use Case, no en JPA)

| Rule | Where Enforced |
|------|----------------|
| Franja MUST be DISPONIBLE to book | `ReservarCitaUseCase` |
| Paciente MUST exist | `ReservarCitaUseCase` (via `PacientePort`) |
| Medico MUST have `disponibleOnline=true` | `ConsultarDisponibilidadUseCase` |
| `horaFin` > `horaInicio` | Domain constructor |
| `fecha` >= hoy | `ReservarCitaUseCase` |
