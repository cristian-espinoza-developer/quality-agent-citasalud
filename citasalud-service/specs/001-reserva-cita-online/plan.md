# Implementation Plan: Reserva de Cita en Línea 24/7

**Branch**: `001-reserva-cita-online` | **Date**: 2026-06-27 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-reserva-cita-online/spec.md`

## Summary

Implementar el módulo de reserva de citas médicas en línea 24/7 (US-01, Épica E-01) sobre el
servicio Spring Boot existente. El paciente autenticado puede consultar disponibilidad de médicos
y reservar una franja horaria, garantizando ausencia de doble reserva mediante bloqueo pesimista
JPA. La confirmación se notifica vía WhatsApp de forma asíncrona.

La implementación sigue **Clean Architecture** (Robert Martin) con generación de código REST a
partir del contrato OpenAPI (`contracts/openapi.yml`), pruebas BDD en tres niveles (unit,
integration, functional) y cobertura JaCoCo ≥ 80%.

## Technical Context

**Language/Version**: Java 25

**Primary Dependencies**:
- Spring Boot 4.1.0 (Web MVC, Data JPA, Validation)
- Lombok (boilerplate reduction)
- openapi-generator-gradle-plugin (server stubs from OpenAPI contract)
- JaCoCo Gradle plugin (coverage measurement and enforcement)
- H2 (testing); PostgreSQL-compatible (production profile — future story)

**Storage**: H2 in-memory (tests) / JPA con Hibernate (ORM)

**Testing**: JUnit 5 + Spring Boot Test (BDD-style con `@Nested` y Given/When/Then en nombres
de método); Mockito para mocks en unit tests

**Target Platform**: JVM — servicio REST sobre Spring Boot 4.1.0, Linux server

**Project Type**: web-service (REST API)

**Performance Goals**:
- Tiempo de respuesta al confirmar reserva: < 3 s en p95 bajo carga normal (SC-006)
- Notificación WhatsApp al paciente: < 60 s tras confirmación (SC-003)

**Constraints**:
- Disponibilidad 24/7, uptime ≥ 99.5% (SC-002)
- Cero dobles reservas (SC-004) — bloqueo pesimista obligatorio
- Código generado por openapi-generator excluido de métricas de cobertura

**Scale/Scope**: MVP — un médico de prueba, múltiples pacientes concurrentes

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Requirement | Status |
|-----------|-------------|--------|
| I. Clean Architecture | Capas definidas: Domain / Application / Adapter / Infrastructure. Dependencias solo hacia adentro. | ✅ |
| II. BDD Testing | Tests unit + integration + functional planificados con Given/When/Then en 3 niveles. Obligatorios. | ✅ |
| III. SOLID/YAGNI/DRY | Sin abstracciones especulativas; responsabilidad única por clase; sin WhatsApp síncrono hasta ser necesario. | ✅ |
| IV. API First | `contracts/openapi.yml` definido antes de implementar; openapi-generator configurado en build. | ✅ |
| V. Coverage & JaCoCo | JaCoCo plugin; per-class > 80%, global ≥ 80%; umbral enforced en `check` task. | ✅ |

**Post-Design Re-check**: ✅ Todos los principios respetados en el diseño de capas y contratos.

**Remediaciones aplicadas**:
- FR-010 (C1): `PatientIdentityExtractor` en adapter/in/web — extrae `pacienteId` del header `X-Patient-Id`
- FR-011 (H2): Spring Boot Actuator `/actuator/health` cubre requisito 24/7 (FR-007)
- H1 resuelto: `X-Idempotency-Key` removido del contrato (YAGNI; sin FR correspondiente)
- H3 resuelto: T070 en Phase N mide baseline p95 < 3s para SC-006

## Project Structure

### Documentation (this feature)

```text
specs/001-reserva-cita-online/
├── plan.md              # Este archivo
├── research.md          # Decisiones de diseño y resolución de clarifications
├── data-model.md        # Modelo de dominio y entidades JPA
├── quickstart.md        # Guía de validación end-to-end
├── contracts/
│   └── openapi.yml      # Contrato OpenAPI 3.1.0 — fuente de verdad de la API
├── checklists/
│   └── requirements.md  # Checklist de calidad de la especificación
└── tasks.md             # Generado por /speckit-tasks (siguiente paso)
```

### Source Code (repository root)

```text
src/
├── main/
│   ├── java/org/ups/citasalud/
│   │   ├── CitasaludServiceApplication.java     # existente
│   │   │
│   │   ├── domain/                              # CAPA 1: Entidades y puertos
│   │   │   ├── model/
│   │   │   │   ├── Cita.java                   # Agregado raíz
│   │   │   │   ├── FranjaHoraria.java
│   │   │   │   ├── Medico.java
│   │   │   │   ├── Paciente.java
│   │   │   │   ├── EstadoCita.java             # Enum
│   │   │   │   └── EstadoFranja.java           # Enum
│   │   │   ├── port/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ReservarCitaUseCase.java
│   │   │   │   │   └── ConsultarDisponibilidadUseCase.java
│   │   │   │   └── out/
│   │   │   │       ├── CitaPort.java
│   │   │   │       ├── FranjaHorariaPort.java
│   │   │   │       ├── MedicoPort.java
│   │   │   │       ├── PacientePort.java
│   │   │   │       └── NotificacionPort.java
│   │   │   └── exception/
│   │   │       ├── FranjaOcupadaException.java
│   │   │       └── RecursoNoEncontradoException.java
│   │   │
│   │   ├── application/                         # CAPA 2: Casos de uso
│   │   │   ├── usecase/
│   │   │   │   ├── ReservarCitaService.java    # impl de ReservarCitaUseCase
│   │   │   │   └── ConsultarDisponibilidadService.java
│   │   │   └── event/
│   │   │       └── CitaConfirmadaEvent.java    # domain event para notificación
│   │   │
│   │   ├── adapter/                             # CAPA 3: Adaptadores
│   │   │   ├── in/
│   │   │   │   └── web/
│   │   │   │       ├── CitaController.java         # implementa delegate generado
│   │   │   │       ├── MedicoController.java
│   │   │   │       ├── GlobalExceptionHandler.java
│   │   │   │       ├── PatientIdentityExtractor.java  # FR-010: extrae pacienteId del header
│   │   │   │       └── mapper/
│   │   │   │           ├── CitaWebMapper.java      # Cita ↔ CitaConfirmadaResponse
│   │   │   │           └── MedicoWebMapper.java    # Medico ↔ MedicoResumen + DisponibilidadMedicoResponse
│   │   │   └── out/
│   │   │       ├── persistence/
│   │   │       │   ├── entity/
│   │   │       │   │   ├── CitaJpaEntity.java
│   │   │       │   │   ├── FranjaHorariaJpaEntity.java
│   │   │       │   │   ├── MedicoJpaEntity.java
│   │   │       │   │   └── PacienteJpaEntity.java
│   │   │       │   ├── repository/
│   │   │       │   │   ├── CitaJpaRepository.java
│   │   │       │   │   ├── FranjaHorariaJpaRepository.java
│   │   │       │   │   ├── MedicoJpaRepository.java
│   │   │       │   │   └── PacienteJpaRepository.java
│   │   │       │   └── adapter/
│   │   │       │       ├── CitaPersistenceAdapter.java
│   │   │       │       ├── FranjaHorariaPersistenceAdapter.java
│   │   │       │       ├── MedicoPersistenceAdapter.java
│   │   │       │       └── PacientePersistenceAdapter.java
│   │   │       └── notification/
│   │   │           └── WhatsAppNotificationAdapter.java
│   │   │
│   │   └── infrastructure/                      # CAPA 4: Configuración y wiring
│   │       └── config/
│   │           ├── ApplicationConfig.java       # beans y DI wiring
│   │           └── OpenApiGeneratorConfig.java  # configuración del delegate
│   │
│   └── resources/
│       ├── application.yaml                     # existente + config SQL init
│       ├── openapi/
│       │   └── openapi.yml                      # copia del contrato para el generador
│       └── db/
│           ├── schema.sql                       # DDL: CREATE TABLE de todas las entidades
│           └── data.sql                         # DML: datos pre-cargados (médicos, pacientes, franjas)
│
└── test/
    ├── java/org/ups/citasalud/
    │   ├── CitasaludServiceApplicationTests.java  # existente
    │   ├── unit/
    │   │   ├── domain/
    │   │   │   └── model/
    │   │   │       ├── CitaTest.java
    │   │   │       └── FranjaHorariaTest.java
    │   │   └── application/
    │   │       └── usecase/
    │   │           ├── ReservarCitaServiceTest.java
    │   │           └── ConsultarDisponibilidadServiceTest.java
    │   ├── integration/
    │   │   └── adapter/
    │   │       ├── persistence/
    │   │       │   ├── CitaPersistenceAdapterIT.java
    │   │       │   └── FranjaHorariaPersistenceAdapterIT.java
    │   │       └── notification/
    │   │           └── WhatsAppNotificationAdapterIT.java
    │   └── functional/
    │       ├── ReservarCitaFunctionalTest.java
    │       └── ConsultarDisponibilidadFunctionalTest.java
    └── resources/
        └── db/
            └── data-test.sql                    # DML: datos adicionales solo para pruebas
```

**Structure Decision**: Single Spring Boot project (web-service). Clean Architecture en 4 capas
dentro del paquete `org.ups.citasalud`. Código REST generado por openapi-generator a partir de
`src/main/resources/openapi/openapi.yml`.

## Design Decisions Summary

| Decision | Choice | Reference |
|----------|--------|-----------|
| BDD testing | JUnit 5 + `@Nested` + Given/When/Then en nombres | research.md #1 |
| Concurrencia | Pessimistic lock (`PESSIMISTIC_WRITE`) | research.md #2 |
| WhatsApp notification | `@TransactionalEventListener(AFTER_COMMIT)` async | research.md #3 |
| API code generation | `openapi-generator-gradle-plugin` — spring delegate | research.md #4 |
| Base de datos test | H2 in-memory + compatibilidad JPA | research.md #5 |
| DB Schema & Data init | `db/schema.sql` (DDL) + `db/data.sql` (DML) cargados por `spring.sql.init` | plan.md §DB Init |
| Coverage enforcement | JaCoCo `jacocoTestCoverageVerification` en `check` task | research.md #6 |

## DB Init Strategy

Spring Boot SQL initialization carga automáticamente los archivos al arrancar:

| Archivo | Tipo | Propósito |
|---------|------|-----------|
| `src/main/resources/db/schema.sql` | DDL | `CREATE TABLE` para medico, paciente, franja_horaria, cita |
| `src/main/resources/db/data.sql` | DML | Datos pre-cargados: médicos habilitados, pacientes de prueba, franjas horarias |
| `src/test/resources/db/data-test.sql` | DML | Datos adicionales específicos de tests (se aplica solo en contexto de test) |

**Configuración en `application.yaml`**:
```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:db/schema.sql
      data-locations: classpath:db/data.sql
  jpa:
    hibernate:
      ddl-auto: none          # schema.sql es la fuente de verdad del DDL
    show-sql: true
  h2:
    console:
      enabled: true           # accesible en /h2-console para debugging local
```

`ddl-auto: none` es intencional: el DDL lo gestiona `schema.sql`, no Hibernate.
Para tests, Spring Boot carga también `src/test/resources/db/data-test.sql` si existe y está
referenciado en la configuración del test (via `@Sql` o `application.yaml` de test).

**Contenido esperado de `schema.sql`** (tablas a crear):
- `medico` (id, nombre, apellido, especialidad, disponible_online)
- `paciente` (id, nombre, apellido, telefono_whatsapp)
- `franja_horaria` (id, medico_id, fecha, hora_inicio, hora_fin, estado, version)
- `cita` (id, paciente_id, franja_horaria_id, estado, fecha_creacion)

**Contenido esperado de `data.sql`** (datos pre-cargados):
- Al menos 2 médicos con `disponible_online = true` (distintas especialidades)
- Al menos 2 pacientes con `telefono_whatsapp` válido (formato E.164)
- Franjas horarias para múltiples fechas futuras (mezcla de DISPONIBLE y OCUPADA)

## Key Dependencies to Add (build.gradle)

```groovy
// OpenAPI Generator
id 'org.openapi.generator' version '7.x'

// JaCoCo
apply plugin: 'jacoco'

// Jakarta Validation
implementation 'org.springframework.boot:spring-boot-starter-validation'

// Spring Boot Actuator (health endpoint — FR-007/FR-011)
implementation 'org.springframework.boot:spring-boot-starter-actuator'

// Jackson — ya incluida vía spring-boot-starter-webmvc
```

## Complexity Tracking

> **No hay violaciones de principios. Tabla no aplica.**

Todos los principios de la Constitución se cumplen en este diseño sin excepciones.
