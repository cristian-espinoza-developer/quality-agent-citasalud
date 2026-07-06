# Research: Reserva de Cita en Línea 24/7

**Feature**: US-01 · 001-reserva-cita-online
**Date**: 2026-06-27
**Status**: Complete — all NEEDS CLARIFICATION resolved

---

## Decision 1: Enfoque de pruebas BDD

**Decision**: JUnit 5 con `@Nested` y nombres de método en formato
`dado_<contexto>_cuando_<accion>_entonces_<resultado>`

**Rationale**: Spring Boot 4.1.0 ya incluye JUnit 5 vía `spring-boot-starter-test`. No se requiere
dependencia adicional. Los `@Nested` permiten agrupar Given/When/Then de manera legible en el IDE y
en reportes de Surefire/Gradle. El equipo puede migrar a Cucumber en una etapa posterior si los
stakeholders requieren reportes en lenguaje natural.

**Alternatives considered**:
- **Cucumber + JUnit 5**: Añade legibilidad para stakeholders no técnicos, pero introduce dos
  dependencias extra (`cucumber-java`, `cucumber-spring`) y una capa de glue code. Descartado por
  YAGNI para esta historia.
- **Spock (Groovy)**: Potente para BDD, pero mezclar Groovy en un proyecto Java puro aumenta la
  complejidad del build. Descartado.

---

## Decision 2: Control de concurrencia para reserva de franja

**Decision**: Bloqueo pesimista (`PESSIMISTIC_WRITE`) al leer la `FranjaHoraria` antes de reservar,
complementado con un índice único en base de datos sobre `(cita_franja_horaria_id)` como segunda
línea de defensa.

**Rationale**: El bloqueo pesimista garantiza que dos transacciones concurrentes que intentan
reservar la misma franja no puedan hacerlo simultáneamente: la primera obtiene el lock, actualiza
el estado a OCUPADA y hace commit; la segunda intentará el lock y encontrará el estado OCUPADA,
lanzando `FranjaOcupadaException`. Sencillo, correcto y comprensible para el equipo.

**Alternatives considered**:
- **Optimistic locking (`@Version`)**: Requiere lógica de retry en el caso de uso. Adecuado para
  alta concurrencia con pocas colisiones, pero la semántica de reserva de citas tiene alta
  probabilidad de colisión (franjas populares). Descartado por ahora.
- **Select + Update sin lock**: Race condition garantizada en carga. Descartado.

---

## Decision 3: Notificación WhatsApp — síncrona vs asíncrona

**Decision**: Asíncrona desacoplada con `ApplicationEventPublisher` de Spring y un
`@TransactionalEventListener(phase = AFTER_COMMIT)`.

**Rationale**: La confirmación de la cita no debe depender de la disponibilidad de la API de
WhatsApp. El `AFTER_COMMIT` garantiza que el evento solo se publica si la transacción de reserva
confirmó exitosamente, eliminando el riesgo de notificar una reserva que falló. Si la notificación
falla, el paciente puede ver su cita en el sistema; la política de reintento es responsabilidad de
la capa de infraestructura.

**Alternatives considered**:
- **Notificación síncrona en el Use Case**: Acoplamiento directo al proveedor de WhatsApp.
  Si la API está caída, la reserva falla aunque los datos sean correctos. Viola SRP. Descartado.
- **Message broker (Kafka/RabbitMQ)**: Robusto para alta escala, pero añade infraestructura
  significativa para este story. YAGNI. Descartado para esta historia.

---

## Decision 4: Generación de código desde OpenAPI

**Decision**: `openapi-generator-gradle-plugin` con generator `spring` (Spring MVC, modo
`useSpringBoot3=true`, interfaz `delegate`).

**Rationale**: El contrato OpenAPI 3.x (en `src/main/resources/openapi/openapi.yml`) es la fuente
de verdad. El plugin genera interfaces Java que los controllers deben implementar. Si el contrato
cambia, la compilación falla si el adapter no se actualiza — enforcement automático de API First.
Los DTOs generados (`*Request`, `*Response`) se usan en la capa de Interface Adapters; nunca
cruzan hacia el dominio.

**Alternatives considered**:
- **Springdoc + anotaciones en el controller**: El código es la fuente de verdad, no el contrato.
  Viola API First. Descartado.
- **Generador TypeScript/client-side**: Necesario más adelante para el frontend; fuera de alcance
  de este story.

---

## Decision 5: Base de datos — H2 vs PostgreSQL

**Decision**: H2 en memoria para todos los tests (unit, integration, functional). PostgreSQL como
target para producción, configurado via perfil `prod` en `application.yaml` (fuera del alcance de
esta historia).

**Rationale**: El proyecto ya tiene H2 configurado. Para los tests de integración de repositorios,
H2 con modo de compatibilidad PostgreSQL (`MODE=PostgreSQL`) es suficiente para validar el
comportamiento de JPA. No se introduce Testcontainers en este story por YAGNI, pero es la ruta
natural de evolución.

**Alternatives considered**:
- **Testcontainers con PostgreSQL real**: Mayor fidelidad, pero añade Docker como requisito del
  entorno de CI y aumenta el tiempo de test. Planificar para la historia de Go-Live.

---

## Decision 6: JaCoCo — configuración de umbrales

**Decision**: Plugin `jacoco` de Gradle con task `jacocoTestCoverageVerification` ejecutada en el
lifecycle `check`. Exclusiones: clases generadas por openapi-generator (`**/openapi/**`) y Lombok
(`**/model/**/*_`).

**Thresholds**:
- Global instrucción: ≥ 80%
- Por clase (BUNDLE): > 80%

**Alternatives considered**:
- **SonarQube**: Complementario, no sustituto de JaCoCo. Se puede integrar más adelante.

---

## Resolved Clarifications

| Item | Resolution |
|------|------------|
| Autenticación del paciente | Se asume JWT Bearer token en header; el `pacienteId` se extrae del token en el adapter. No forma parte de este story. |
| Duración de franja | Parámetro de configuración (`citasalud.slot.duration-minutes=30`). Default: 30 min. |
| Número de franjas por día | Determinado por la agenda del médico (pre-configurada por admin). |
| Retry de notificación WhatsApp | Máximo 3 reintentos con backoff exponencial en `WhatsAppNotificationAdapter`. |
| Idempotencia de confirmación | Se maneja con clave de idempotencia opcional en el header `X-Idempotency-Key`. |
