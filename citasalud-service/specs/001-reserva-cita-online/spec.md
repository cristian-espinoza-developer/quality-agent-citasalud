# Feature Specification: Reserva de Cita en Línea 24/7

**Feature Branch**: `001-reserva-cita-online`

**Epic**: E-01 · Reserva de Cita en Línea · **Story Points**: 8

**User Story**: US-01

**Created**: 2026-06-27

**Status**: Draft

**Input**: Como paciente, quiero reservar una cita en línea en cualquier momento del día,
para no tener que llamar durante mi horario de almuerzo ni acumular intentos fallidos.

## User Scenarios & Testing *(mandatory — BDD required per Constitution Principle II)*

### User Story 1 — Reserva exitosa fuera de horario de atención telefónica (Priority: P1)

El paciente ingresa al sistema en cualquier momento del día (incluso fuera del horario de atención
telefónica), selecciona un médico, elige una fecha y una franja horaria disponible, confirma la
reserva y recibe una notificación de confirmación por WhatsApp.

**Why this priority**: Es el flujo de valor principal. Sin este flujo, la historia no tiene sentido.
Permite al paciente librarse de la dependencia del horario telefónico.

**Independent Test**: Se puede verificar registrando un paciente, ejecutando el flujo completo de
selección y confirmación fuera de horario, y validando que la cita aparece en el sistema y se
recibe el WhatsApp.

**Acceptance Scenarios**:

1. **Given** el paciente está autenticado en el sistema y accede al módulo de reservas fuera del
   horario de atención telefónica (ej. 22:00 h),
   **When** selecciona un médico disponible, elige una fecha y una franja horaria libre y confirma
   la reserva,
   **Then** la cita queda registrada en el sistema con estado "confirmada", la franja horaria queda
   marcada como ocupada y el paciente recibe un mensaje de confirmación por WhatsApp con los
   detalles de la cita (médico, fecha, hora, lugar).

2. **Given** el paciente ha completado la selección de médico, fecha y hora,
   **When** confirma la reserva,
   **Then** el sistema responde con un mensaje de éxito en menos de 3 segundos y la cita es
   visible en la agenda del médico.

---

### User Story 2 — Intento de reserva en franja ya ocupada (Priority: P1)

El paciente intenta seleccionar una franja horaria que ya está ocupada y el sistema le impide
confirmarla, mostrando un mensaje claro e invitándolo a elegir otra opción.

**Why this priority**: Previene doble reserva (dato de negocio crítico) y define la experiencia
ante concurrencia.

**Independent Test**: Se puede verificar reservando una franja, luego intentando reservar la misma
franja con otro paciente (o la misma sesión) y confirmando que el sistema bloquea la acción.

**Acceptance Scenarios**:

1. **Given** una franja horaria ya está ocupada por otro paciente,
   **When** el paciente intenta seleccionar esa misma franja y confirmarla,
   **Then** el sistema muestra la franja como no disponible (visualmente diferenciada) y no
   permite confirmarla; además, presenta un mensaje invitando al paciente a elegir otra franja.

2. **Given** dos pacientes intentan reservar la misma franja simultáneamente,
   **When** ambos confirman al mismo tiempo,
   **Then** solo uno de los dos obtiene la reserva; el otro recibe un mensaje indicando que la
   franja fue ocupada y es invitado a seleccionar una alternativa; no se produce doble reserva.

---

### User Story 3 — Consulta de disponibilidad por médico y fecha (Priority: P2)

El paciente puede explorar los horarios disponibles de un médico en una fecha determinada antes
de confirmar una reserva.

**Why this priority**: Depende de P1 para tener valor, pero es necesario para el flujo completo.

**Independent Test**: Se puede verificar consultando la disponibilidad de un médico con agenda
parcialmente ocupada y validando que el sistema muestra correctamente las franjas libres y ocupadas.

**Acceptance Scenarios**:

1. **Given** el paciente selecciona un médico y una fecha,
   **When** el sistema recupera la agenda de ese médico para esa fecha,
   **Then** se muestran las franjas horarias disponibles (libres) diferenciadas visualmente de las
   no disponibles (ocupadas o fuera de agenda); las franjas ocupadas no son seleccionables.

2. **Given** el médico no tiene franjas disponibles para la fecha seleccionada,
   **When** el paciente consulta la disponibilidad,
   **Then** el sistema informa que no hay disponibilidad para esa fecha y sugiere cambiar de fecha
   o de médico.

---

### Edge Cases

- ¿Qué ocurre si la conexión se pierde justo después de confirmar pero antes de recibir la
  respuesta del servidor? El sistema DEBE garantizar idempotencia: la cita no debe quedar
  duplicada si el paciente reintenta.
- ¿Qué ocurre si el servicio de WhatsApp no está disponible en el momento de la confirmación?
  La reserva queda registrada de todas formas; la notificación se reintenta de forma asíncrona.
- ¿Qué ocurre si el médico cancela su agenda después de que el paciente ya reservó? Esa situación
  es manejada por un flujo separado de cancelación/notificación (fuera del alcance de esta historia).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir al paciente autenticado navegar la lista de médicos
  disponibles para reserva en línea.
- **FR-002**: El sistema DEBE mostrar el calendario de disponibilidad de un médico seleccionado,
  diferenciando franjas libres de franjas ocupadas.
- **FR-003**: El sistema DEBE permitir al paciente seleccionar médico, fecha y franja horaria
  disponible para realizar una reserva.
- **FR-004**: El sistema DEBE validar la disponibilidad de la franja en el momento exacto de la
  confirmación (control de concurrencia) para evitar doble reserva.
- **FR-005**: El sistema DEBE registrar la cita con estado "confirmada" y marcar la franja como
  ocupada de forma atómica al confirmar.
- **FR-006**: El sistema DEBE enviar una notificación de confirmación por WhatsApp al paciente
  con los detalles de la cita (médico, especialidad, fecha, hora, lugar/modalidad) dentro de
  los 60 segundos siguientes a la confirmación.
- **FR-007**: El sistema DEBE estar disponible para reservas las 24 horas del día, los 7 días
  de la semana, sin ventanas de mantenimiento que bloqueen la funcionalidad de reserva.
- **FR-008**: El sistema DEBE presentar un mensaje claro y una invitación a elegir otra franja
  cuando el paciente intente confirmar una ya ocupada.
- **FR-009**: El sistema DEBE garantizar que una franja horaria no pueda ser reservada por más
  de un paciente (unicidad de reserva).
- **FR-010**: El sistema DEBE identificar al paciente que realiza la reserva extrayendo su
  identificador desde el contexto de autenticación — en producción, desde el claim `sub` del
  token JWT Bearer; en desarrollo/MVP, desde el header `X-Patient-Id` como stub. El
  `pacienteId` NUNCA se acepta como parámetro del cuerpo de la solicitud.
- **FR-011**: El sistema DEBE exponer un endpoint de salud (`/actuator/health`) que retorne
  estado `UP` cuando el servicio esté operativo, permitiendo monitoreo de disponibilidad 24/7
  (FR-007) sin intervención manual.

### Key Entities

- **Paciente**: Actor principal. Persona registrada en el sistema que realiza la reserva.
  Atributos relevantes: identificador, nombre, número de WhatsApp.
- **Médico**: Profesional de salud cuya agenda se consulta. Atributos: identificador, nombre,
  especialidad, disponibilidad configurada.
- **FranjaHoraria**: Bloque de tiempo dentro de la agenda de un médico. Estado: disponible /
  ocupada. Atributos: médico, fecha, hora inicio, hora fin, estado.
- **Cita**: Reserva confirmada que asocia un paciente a una franja horaria de un médico.
  Estado: confirmada / cancelada / completada.
- **NotificacionConfirmacion**: Mensaje enviado al paciente vía WhatsApp con los detalles de
  la cita confirmada.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El paciente puede completar el proceso de reserva (desde selección de médico hasta
  confirmación) en menos de 3 minutos.
- **SC-002**: El sistema está disponible para reservas 24/7 con al menos 99.5% de uptime mensual.
- **SC-003**: La notificación de confirmación por WhatsApp llega al paciente en menos de 60
  segundos tras la confirmación.
- **SC-004**: Tasa de doble reserva para la misma franja y médico: 0% (cero dobles reservas).
- **SC-005**: El 90% de los pacientes completa el proceso de reserva sin necesidad de asistencia
  externa.
- **SC-006**: El tiempo de respuesta del sistema al confirmar una reserva es menor a 3 segundos
  en el percentil 95 bajo carga normal.

## Assumptions

- El paciente ya está autenticado en el sistema antes de acceder al módulo de reservas
  (la autenticación/registro es una historia separada fuera del alcance de US-01).
- La integración con WhatsApp Business API está disponible como canal de notificación;
  el número de WhatsApp del paciente está registrado en su perfil.
- Los horarios y agendas de los médicos son configurados previamente por el personal
  administrativo; esta historia no incluye la gestión de agendas por parte del médico.
- El sistema opera sobre una arquitectura que soporta acceso concurrente de múltiples
  pacientes intentando reservar simultáneamente.
- La disponibilidad se define en franjas de duración fija (ej. 30 minutos); la duración
  estándar de franja es un parámetro de configuración del sistema.
- Las notificaciones fallidas por indisponibilidad de WhatsApp se reintentan de forma
  asíncrona; el paciente puede ver su cita confirmada en el sistema independientemente
  del estado de la notificación.
- El flujo de cancelación y reprogramación de citas está fuera del alcance de esta historia.
