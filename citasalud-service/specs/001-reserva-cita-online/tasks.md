---
description: "Task list for US-01 Reserva de Cita en Línea 24/7"
---

# Tasks: Reserva de Cita en Línea 24/7

**Input**: Design documents from `specs/001-reserva-cita-online/`

**Prerequisites**: plan.md ✅ | spec.md ✅ | data-model.md ✅ | contracts/openapi.yml ✅ | research.md ✅

**Tests**: MANDATORY per Constitution Principle II — unit, integration y functional tests BDD
(Given/When/Then) incluidos en cada User Story.

**Organization**: Tasks agrupadas por User Story para habilitar implementación y prueba independiente.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias bloqueantes)
- **[Story]**: User Story a la que pertenece la tarea (US1, US2, US3)
- Todos los paths son relativos a la raíz del proyecto

## Path Conventions

```text
src/main/java/org/ups/citasalud/    → código principal
src/test/java/org/ups/citasalud/    → pruebas Java
src/main/resources/                  → recursos principales (YAML, SQL, OpenAPI)
src/test/resources/                  → recursos de prueba
```

---

## Phase 1: Setup (Infraestructura compartida)

**Purpose**: Inicializar el proyecto con las herramientas requeridas por la Constitución
(openapi-generator, JaCoCo, validación, Actuator) y preparar esquema de BD y datos pre-cargados.

- [X] T001 Configurar `openapi-generator-gradle-plugin` en `build.gradle` (generator: `spring`, outputDir: `build/generated`, apiPackage: `org.ups.citasalud.openapi.api`, modelPackage: `org.ups.citasalud.openapi.model`, `useSpringBoot3=true`, `interfaceOnly=true`) y agregar `compileJava.dependsOn openApiGenerate`
- [X] T002 [P] Configurar plugin `jacoco` en `build.gradle`: task `jacocoTestCoverageVerification` con instrucción global ≥ 80% y por clase (BUNDLE) > 80%; excluir `org/ups/citasalud/openapi/**`; hacer que `check` dependa de `jacocoTestCoverageVerification`
- [X] T003 [P] Agregar dependencias en `build.gradle`: `spring-boot-starter-validation` y `spring-boot-starter-actuator`
- [X] T004 Copiar `specs/001-reserva-cita-online/contracts/openapi.yml` a `src/main/resources/openapi/openapi.yml` (fuente para openapi-generator)
- [X] T005 Crear `src/main/resources/db/schema.sql` con `CREATE TABLE IF NOT EXISTS` para las 4 tablas: `medico` (id UUID PK, nombre, apellido, especialidad, disponible_online BOOLEAN), `paciente` (id UUID PK, nombre, apellido, telefono_whatsapp), `franja_horaria` (id UUID PK, medico_id UUID FK, fecha DATE, hora_inicio TIME, hora_fin TIME, estado VARCHAR, version BIGINT), `cita` (id UUID PK, paciente_id UUID FK, franja_horaria_id UUID FK UNIQUE, estado VARCHAR, fecha_creacion TIMESTAMP); índice único en `franja_horaria(medico_id, fecha, hora_inicio)`
- [X] T006 [P] Crear `src/main/resources/db/data.sql` con INSERTs de: 2 médicos con `disponible_online=TRUE` (especialidades distintas), 2 pacientes con `telefono_whatsapp` en formato E.164 (+593XXXXXXXXX), franjas horarias para 2026-07-15 (mezcla DISPONIBLE/OCUPADA) y 2026-08-01
- [X] T007 [P] Crear `src/test/resources/db/data-test.sql` con datos adicionales exclusivos para tests (3er paciente para concurrencia; franja OCUPADA lista para test 409) y `src/test/resources/application-test.yaml` con `spring.sql.init.data-locations: classpath:db/data.sql,classpath:db/data-test.sql`
- [X] T008 [P] Actualizar `src/main/resources/application.yaml`: `spring.sql.init.mode: always`, `schema-locations: classpath:db/schema.sql`, `data-locations: classpath:db/data.sql`, `jpa.hibernate.ddl-auto: none`, `show-sql: true`, `h2.console.enabled: true`, `management.endpoints.web.exposure.include: health`, `management.endpoint.health.show-details: always`
- [X] T009 [P] Escribir functional test `ActuatorHealthFunctionalTest` — `cuandoGetActuatorHealth_entoncesRetorna200ConStatusUp` en `src/test/java/org/ups/citasalud/functional/ActuatorHealthFunctionalTest.java` (cubre FR-007 y FR-011)

**Checkpoint**: `./gradlew build --info` compila; `build/generated/` contiene fuentes OpenAPI; `GET /actuator/health` → `{"status":"UP"}`; H2 console accesible en `/h2-console`

---

## Phase 2: Foundational (Prerequisitos bloqueantes para todas las US)

**Purpose**: Esqueleto de Clean Architecture — modelos de dominio, puertos, entidades JPA,
repositorios Spring Data y configuración de DI. Todo lo que bloquea el inicio de cualquier US.

**CRÍTICO**: No puede comenzarse ninguna User Story hasta completar esta fase.

- [X] T010 Crear enum `EstadoCita` (CONFIRMADA, CANCELADA, COMPLETADA) en `src/main/java/org/ups/citasalud/domain/model/EstadoCita.java`
- [X] T011 [P] Crear enum `EstadoFranja` (DISPONIBLE, OCUPADA) en `src/main/java/org/ups/citasalud/domain/model/EstadoFranja.java`
- [X] T012 Crear domain model `Cita` (id UUID, pacienteId UUID, franjaHoraria FranjaHoraria, estado EstadoCita, fechaCreacion LocalDateTime; constructor privado, factory method `confirmar(...)`) en `src/main/java/org/ups/citasalud/domain/model/Cita.java`
- [X] T013 [P] Crear domain model `FranjaHoraria` (id UUID, medicoId UUID, fecha LocalDate, horaInicio LocalTime, horaFin LocalTime, estado EstadoFranja) con método `reservar()` que lanza `FranjaOcupadaException` si `estado == OCUPADA` y cambia estado a OCUPADA en `src/main/java/org/ups/citasalud/domain/model/FranjaHoraria.java`
- [X] T014 [P] Crear domain model `Medico` (id UUID, nombre String, apellido String, especialidad String, disponibleOnline boolean) en `src/main/java/org/ups/citasalud/domain/model/Medico.java`
- [X] T015 [P] Crear domain model `Paciente` (id UUID, nombre String, apellido String, telefonoWhatsApp String) en `src/main/java/org/ups/citasalud/domain/model/Paciente.java`
- [X] T016 [P] Crear value object `NotificacionConfirmacion` (citaId UUID, pacienteNombreCompleto String, telefonoWhatsApp String, medicoNombreCompleto String, medicoEspecialidad String, fecha LocalDate, horaInicio LocalTime, horaFin LocalTime) en `src/main/java/org/ups/citasalud/domain/model/NotificacionConfirmacion.java`
- [X] T017 Crear `FranjaOcupadaException` (RuntimeException, campo `franjaId UUID`) en `src/main/java/org/ups/citasalud/domain/exception/FranjaOcupadaException.java`
- [X] T018 [P] Crear `RecursoNoEncontradoException` (RuntimeException, campo `mensaje String`) en `src/main/java/org/ups/citasalud/domain/exception/RecursoNoEncontradoException.java`
- [X] T019 Crear input port `ReservarCitaUseCase` (interface: `Cita reservar(UUID pacienteId, UUID franjaHorariaId)`) en `src/main/java/org/ups/citasalud/domain/port/in/ReservarCitaUseCase.java`
- [X] T020 [P] Crear input port `ConsultarDisponibilidadUseCase` (interface: `List<Medico> listarMedicosDisponibles(String especialidad)` y `List<FranjaHoraria> consultarDisponibilidad(UUID medicoId, LocalDate fecha)`) en `src/main/java/org/ups/citasalud/domain/port/in/ConsultarDisponibilidadUseCase.java`
- [X] T021 [P] Crear output port `CitaPort` (interface: `Cita guardar(Cita)` y `Optional<Cita> buscarPorId(UUID)`) en `src/main/java/org/ups/citasalud/domain/port/out/CitaPort.java`
- [X] T022 [P] Crear output port `FranjaHorariaPort` (interface: `Optional<FranjaHoraria> buscarConLockPesimista(UUID)`, `FranjaHoraria guardar(FranjaHoraria)`, `List<FranjaHoraria> buscarPorMedicoYFecha(UUID, LocalDate)`) en `src/main/java/org/ups/citasalud/domain/port/out/FranjaHorariaPort.java`
- [X] T023 [P] Crear output port `MedicoPort` (interface: `List<Medico> listarDisponiblesOnline(String)`, `Optional<Medico> buscarPorId(UUID)`) en `src/main/java/org/ups/citasalud/domain/port/out/MedicoPort.java`
- [X] T024 [P] Crear output port `PacientePort` (interface: `Optional<Paciente> buscarPorId(UUID)`) en `src/main/java/org/ups/citasalud/domain/port/out/PacientePort.java`
- [X] T025 [P] Crear output port `NotificacionPort` (interface: `void notificarConfirmacion(NotificacionConfirmacion)`) en `src/main/java/org/ups/citasalud/domain/port/out/NotificacionPort.java`
- [X] T026 Crear `CitaConfirmadaEvent` (Spring ApplicationEvent con campo `NotificacionConfirmacion payload`) en `src/main/java/org/ups/citasalud/application/event/CitaConfirmadaEvent.java`
- [X] T027 Crear JPA entity `CitaJpaEntity` (@Entity @Table(name="cita"), campos: id UUID @Id, pacienteId UUID, franjaHorariaId UUID, estado String, fechaCreacion LocalDateTime) en `src/main/java/org/ups/citasalud/adapter/out/persistence/entity/CitaJpaEntity.java`
- [X] T028 [P] Crear JPA entity `FranjaHorariaJpaEntity` (@Entity @Table(name="franja_horaria"), campos: id UUID @Id, medicoId UUID, fecha LocalDate, horaInicio LocalTime, horaFin LocalTime, estado String, @Version Long version) en `src/main/java/org/ups/citasalud/adapter/out/persistence/entity/FranjaHorariaJpaEntity.java`
- [X] T029 [P] Crear JPA entity `MedicoJpaEntity` (@Entity @Table(name="medico"), campos: id UUID @Id, nombre, apellido, especialidad String, disponibleOnline boolean) en `src/main/java/org/ups/citasalud/adapter/out/persistence/entity/MedicoJpaEntity.java`
- [X] T030 [P] Crear JPA entity `PacienteJpaEntity` (@Entity @Table(name="paciente"), campos: id UUID @Id, nombre, apellido, telefonoWhatsApp String) en `src/main/java/org/ups/citasalud/adapter/out/persistence/entity/PacienteJpaEntity.java`
- [X] T031 Crear `CitaJpaRepository` (JpaRepository<CitaJpaEntity, UUID>) en `src/main/java/org/ups/citasalud/adapter/out/persistence/repository/CitaJpaRepository.java`
- [X] T032 [P] Crear `FranjaHorariaJpaRepository` (JpaRepository; `@Lock(PESSIMISTIC_WRITE) @Query("SELECT f FROM FranjaHorariaJpaEntity f WHERE f.id = :id") Optional<FranjaHorariaJpaEntity> findByIdForUpdate(@Param("id") UUID id)` y `List<FranjaHorariaJpaEntity> findByMedicoIdAndFecha(UUID, LocalDate)`) en `src/main/java/org/ups/citasalud/adapter/out/persistence/repository/FranjaHorariaJpaRepository.java`
- [X] T033 [P] Crear `MedicoJpaRepository` (JpaRepository; `findByDisponibleOnlineTrue()` y `findByDisponibleOnlineTrueAndEspecialidadContainingIgnoreCase(String)`) en `src/main/java/org/ups/citasalud/adapter/out/persistence/repository/MedicoJpaRepository.java`
- [X] T034 [P] Crear `PacienteJpaRepository` (JpaRepository<PacienteJpaEntity, UUID>) en `src/main/java/org/ups/citasalud/adapter/out/persistence/repository/PacienteJpaRepository.java`
- [X] T035 Crear `ApplicationConfig` (@Configuration) con @Bean que instancian `ReservarCitaService(CitaPort, FranjaHorariaPort, PacientePort, ApplicationEventPublisher)` y `ConsultarDisponibilidadService(MedicoPort, FranjaHorariaPort)` en `src/main/java/org/ups/citasalud/infrastructure/config/ApplicationConfig.java`

**Checkpoint**: `./gradlew compileJava` pasa sin errores; H2 console muestra las 4 tablas con datos de `data.sql`

---

## Phase 3: User Story 1 — Reserva Exitosa (Priority: P1) 🎯 MVP

**Goal**: Paciente autenticado reserva una franja disponible; cita CONFIRMADA, franja OCUPADA,
notificación WhatsApp asíncrona publicada.

**Independent Test**: `POST /api/v1/citas` (header `X-Patient-Id`) con franja disponible → HTTP 201 + `CitaConfirmadaResponse` + franja OCUPADA en DB + evento notificación publicado

### Tests BDD para US1 (MANDATORY — Constitution Principle II)

> **ESCRIBIR PRIMERO — confirmar que FALLAN (red phase) antes de implementar**

- [X] T036 [US1] Escribir unit test `CitaTest` — `dadoCitaCreada_cuandoConsultarEstado_entoncesRetornaConfirmada` en `src/test/java/org/ups/citasalud/unit/domain/model/CitaTest.java`
- [X] T037 [P] [US1] Escribir unit test `FranjaHorariaTest` — `dadaFranjaDisponible_cuandoReservar_entoncesEstadoCambiaAOcupada` en `src/test/java/org/ups/citasalud/unit/domain/model/FranjaHorariaTest.java`
- [X] T038 [US1] Escribir unit test `ReservarCitaServiceTest` — `dadoPacienteValidoYFranjaDisponible_cuandoReservar_entoncesRetornaCitaConfirmadaYPublicaEvento` usando Mockito para ports en `src/test/java/org/ups/citasalud/unit/application/usecase/ReservarCitaServiceTest.java`
- [X] T039 [US1] Escribir integration test `CitaPersistenceAdapterIT` — `dadaCitaValida_cuandoGuardar_entoncesPersisteEnH2YRecuperaPorId` con datos de `data-test.sql` en `src/test/java/org/ups/citasalud/integration/adapter/persistence/CitaPersistenceAdapterIT.java`
- [X] T040 [P] [US1] Escribir integration test `FranjaHorariaPersistenceAdapterIT` — `dadaFranjaDisponible_cuandoBuscarConLockPesimista_entoncesRetornaFranjaYPermiteCambioEstado` en `src/test/java/org/ups/citasalud/integration/adapter/persistence/FranjaHorariaPersistenceAdapterIT.java`
- [X] T041 [US1] Escribir functional test `ReservarCitaFunctionalTest` — `dadoPacienteConHeaderXPatientId_cuandoPostApiV1Citas_entoncesRetorna201ConCitaConfirmada` en `src/test/java/org/ups/citasalud/functional/ReservarCitaFunctionalTest.java`

### Implementación US1

- [X] T042 [US1] Implementar `PatientIdentityExtractor` (@Component, FR-010): lee `pacienteId` del header HTTP `X-Patient-Id` como UUID; lanza `RecursoNoEncontradoException` si el header está ausente o malformado; documentar Javadoc que en producción debe reemplazarse por extracción del claim `sub` del JWT en `src/main/java/org/ups/citasalud/adapter/in/web/PatientIdentityExtractor.java`
- [X] T043 [US1] Implementar `ReservarCitaService` (implements ReservarCitaUseCase): (1) validar paciente vía PacientePort, (2) obtener franja con `FranjaHorariaPort.buscarConLockPesimista()`, (3) llamar `franja.reservar()`, (4) guardar franja actualizada, (5) crear y guardar Cita, (6) publicar `CitaConfirmadaEvent` en `src/main/java/org/ups/citasalud/application/usecase/ReservarCitaService.java`
- [X] T044 [US1] Implementar `CitaPersistenceAdapter` (implements CitaPort) con mapeo `Cita ↔ CitaJpaEntity` (método privado, sin MapStruct por YAGNI) en `src/main/java/org/ups/citasalud/adapter/out/persistence/adapter/CitaPersistenceAdapter.java`
- [X] T045 [US1] Implementar `FranjaHorariaPersistenceAdapter` (implements FranjaHorariaPort): `buscarConLockPesimista()` usa `findByIdForUpdate()`; `guardar()` y `buscarPorMedicoYFecha()` delegados al repo en `src/main/java/org/ups/citasalud/adapter/out/persistence/adapter/FranjaHorariaPersistenceAdapter.java`
- [X] T046 [P] [US1] Implementar `PacientePersistenceAdapter` (implements PacientePort) con mapeo `Paciente ↔ PacienteJpaEntity` en `src/main/java/org/ups/citasalud/adapter/out/persistence/adapter/PacientePersistenceAdapter.java`
- [X] T047 [US1] Implementar `WhatsAppNotificationAdapter` (implements NotificacionPort) con `@TransactionalEventListener(phase = AFTER_COMMIT)`: loguear payload y simular HTTP call (stub MVP — retry max 3 veces con backoff exponencial; nota: real WhatsApp Business API es historia futura) en `src/main/java/org/ups/citasalud/adapter/out/notification/WhatsAppNotificationAdapter.java`
- [X] T048 [US1] Implementar `CitaWebMapper` — `toResponse(Cita, Medico): CitaConfirmadaResponse`; mapeo manual domain → DTO generado en `src/main/java/org/ups/citasalud/adapter/in/web/mapper/CitaWebMapper.java`
- [X] T049 [US1] Implementar `CitaController` (implements delegate OpenAPI generado): inyectar `PatientIdentityExtractor`; `reservarCita(req)` → extrae `pacienteId`, llama use case, retorna 201; `obtenerCita(id)` → busca y retorna 200 en `src/main/java/org/ups/citasalud/adapter/in/web/CitaController.java`

**Checkpoint**: `./gradlew test --tests "*.ReservarCitaFunctionalTest"` verde ✅ — US1 completamente funcional

---

## Phase 4: User Story 2 — Franja ya Ocupada (Priority: P1)

**Goal**: Sistema previene doble reserva: 409 cuando franja OCUPADA o dos pacientes concurren;
mensaje claro invitando a elegir otra franja.

**Independent Test**: `POST /api/v1/citas` con franja ocupada → HTTP 409 + `{"codigo":"FRANJA_OCUPADA",...}`

### Tests BDD para US2 (MANDATORY — Constitution Principle II)

> **ESCRIBIR PRIMERO — confirmar que FALLAN (red phase) antes de implementar**

- [X] T050 [US2] Agregar escenario a `ReservarCitaServiceTest` — `dadaFranjaOcupada_cuandoReservar_entoncesLanzaFranjaOcupadaException` en `src/test/java/org/ups/citasalud/unit/application/usecase/ReservarCitaServiceTest.java`
- [X] T051 [P] [US2] Agregar escenario a `FranjaHorariaTest` — `dadaFranjaOcupada_cuandoReservar_entoncesLanzaFranjaOcupadaException` en `src/test/java/org/ups/citasalud/unit/domain/model/FranjaHorariaTest.java`
- [X] T052 [US2] Agregar escenario a `ReservarCitaFunctionalTest` — `dadaFranjaYaOcupada_cuandoPostApiV1Citas_entoncesRetorna409ConCodigoFranjaOcupada` en `src/test/java/org/ups/citasalud/functional/ReservarCitaFunctionalTest.java`
- [X] T053 [P] [US2] Agregar escenario a `ReservarCitaFunctionalTest` — concurrencia: `dadoDosPacientesConfirmanMismaFranjaConcurrentemente_entoncesUnoRetorna201YOtroRetorna409` en `src/test/java/org/ups/citasalud/functional/ReservarCitaFunctionalTest.java`

### Implementación US2

- [X] T054 [US2] Implementar `GlobalExceptionHandler` (@RestControllerAdvice): `FranjaOcupadaException` → 409 `ErrorResponse(codigo="FRANJA_OCUPADA")`; `RecursoNoEncontradoException` → 404 `ErrorResponse(codigo="NO_ENCONTRADO")`; `MethodArgumentNotValidException` → 400 en `src/main/java/org/ups/citasalud/adapter/in/web/GlobalExceptionHandler.java`

**Checkpoint**: `./gradlew test --tests "*.ReservarCitaFunctionalTest"` todos los escenarios verde ✅ — US1 + US2 funcionales

---

## Phase 5: User Story 3 — Consulta de Disponibilidad (Priority: P2)

**Goal**: Paciente explora médicos online y sus franjas para una fecha; disponibles y ocupadas
visualmente diferenciadas.

**Independent Test**: `GET /api/v1/medicos` → 200 lista | `GET /api/v1/medicos/{id}/disponibilidad?fecha=2026-07-15` → 200 con franjas y campo `disponible`

### Tests BDD para US3 (MANDATORY — Constitution Principle II)

> **ESCRIBIR PRIMERO — confirmar que FALLAN (red phase) antes de implementar**

- [X] T055 [US3] Escribir unit test `ConsultarDisponibilidadServiceTest` — `dadoMedicoConFranjasDisponibles_cuandoConsultarDisponibilidad_entoncesRetornaFranjasConEstado` en `src/test/java/org/ups/citasalud/unit/application/usecase/ConsultarDisponibilidadServiceTest.java`
- [X] T056 [P] [US3] Agregar escenario — `dadoMedicoSinFranjasParaFecha_cuandoConsultar_entoncesRetornaListaVacia` en `src/test/java/org/ups/citasalud/unit/application/usecase/ConsultarDisponibilidadServiceTest.java`
- [X] T057 [US3] Escribir integration test `MedicoPersistenceAdapterIT` — `dadoMedicoConDisponibleOnlineTrue_cuandoListar_entoncesApareceCEnLista` con datos de `data.sql` en `src/test/java/org/ups/citasalud/integration/adapter/persistence/MedicoPersistenceAdapterIT.java`
- [X] T058 [P] [US3] Escribir integration test `WhatsAppNotificationAdapterIT` — `dadoCitaConfirmadaEvent_cuandoOnEvent_entoncesLogNotificacionSinExcepcion` en `src/test/java/org/ups/citasalud/integration/adapter/notification/WhatsAppNotificationAdapterIT.java`
- [X] T059 [US3] Escribir functional test `ConsultarDisponibilidadFunctionalTest` — `dadoPacienteAuth_cuandoGetApiV1Medicos_entoncesRetorna200ConListaMedicos` en `src/test/java/org/ups/citasalud/functional/ConsultarDisponibilidadFunctionalTest.java`
- [X] T060 [P] [US3] Agregar escenario — `dadoMedicoConFranjas_cuandoGetDisponibilidad_entoncesRetorna200ConFranjasYCampoDisponible` en `src/test/java/org/ups/citasalud/functional/ConsultarDisponibilidadFunctionalTest.java`
- [X] T061 [P] [US3] Agregar escenario — `dadoMedicoSinFranjasEnFechaFutura_cuandoGetDisponibilidad_entoncesRetorna200ListaVacia` en `src/test/java/org/ups/citasalud/functional/ConsultarDisponibilidadFunctionalTest.java`

### Implementación US3

- [X] T062 [US3] Implementar `ConsultarDisponibilidadService` (implements ConsultarDisponibilidadUseCase): `listarMedicosDisponibles(especialidad)` → MedicoPort; `consultarDisponibilidad(medicoId, fecha)` → validar médico, delegar a FranjaHorariaPort en `src/main/java/org/ups/citasalud/application/usecase/ConsultarDisponibilidadService.java`
- [X] T063 [US3] Implementar `MedicoPersistenceAdapter` (implements MedicoPort) con mapeo `Medico ↔ MedicoJpaEntity` en `src/main/java/org/ups/citasalud/adapter/out/persistence/adapter/MedicoPersistenceAdapter.java`
- [X] T064 [P] [US3] Implementar `MedicoWebMapper` — `toResumen(Medico): MedicoResumen`; `toDisponibilidadResponse(UUID, LocalDate, List<FranjaHoraria>): DisponibilidadMedicoResponse`; campo `disponible` de cada franja según `EstadoFranja.DISPONIBLE` en `src/main/java/org/ups/citasalud/adapter/in/web/mapper/MedicoWebMapper.java`
- [X] T065 [US3] Implementar `MedicoController` (implements delegate OpenAPI generado): `listarMedicosDisponibles(especialidad)` → 200 con lista; `consultarDisponibilidad(medicoId, fecha)` → 200 con franjas en `src/main/java/org/ups/citasalud/adapter/in/web/MedicoController.java`

**Checkpoint**: `./gradlew test` todos verde ✅ — 3 User Stories completamente funcionales

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Verificación de cobertura, calidad de código, validación E-E y baseline de rendimiento.

- [X] T066 Ejecutar `./gradlew clean test jacocoTestReport jacocoTestCoverageVerification` — confirmar cobertura global ≥ 80% y por clase > 80%; revisar `build/reports/jacoco/test/html/index.html`; corregir gaps si los hay
- [X] T067 [P] Revisar todas las clases en `domain/` y `application/` para violaciones SOLID/YAGNI/DRY; corregir cualquiera encontrada
- [ ] T068 [P] Ejecutar escenarios de `specs/001-reserva-cita-online/quickstart.md` con `./gradlew bootRun` (curl §3, §4, §5); documentar resultados en la sección "Resultados de Validación" del quickstart
- [ ] T069 [P] Verificar H2 console (`http://localhost:8080/h2-console`): tablas creadas desde `schema.sql`, datos de `data.sql` presentes; confirmar índice único en `franja_horaria` intentando INSERT duplicado (medico_id + fecha + hora_inicio iguales → debe rechazar)
- [ ] T070 Medir baseline de rendimiento de SC-006 (< 3s p95): lanzar `./gradlew bootRun`, ejecutar 20 veces `curl -w "%{time_total}\n" -s -o /dev/null -X POST http://localhost:8080/api/v1/citas ...`, calcular p95; documentar el resultado en `specs/001-reserva-cita-online/quickstart.md` sección "Baseline de Rendimiento" — si p95 > 3s, abrir defecto

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — iniciar de inmediato
- **Foundational (Phase 2)**: Depende de Phase 1 completa — BLOQUEA todas las US
- **US1 (Phase 3)**: Depende de Phase 2 — primer incremento MVP
- **US2 (Phase 4)**: Depende de US1 (comparte `ReservarCitaService` y test class)
- **US3 (Phase 5)**: Depende de Phase 2 — puede ejecutarse **en paralelo** con Phase 4
- **Polish (Phase N)**: Depende de Phase 3 + 4 + 5

### User Story Dependencies

- **US1 (P1)**: Puede iniciar tras Foundational — sin dependencias entre US
- **US2 (P1)**: Comparte implementación con US1; agrega error paths y GlobalExceptionHandler
- **US3 (P2)**: Puede iniciar tras Foundational — independiente de US1/US2

### Within Each User Story

1. Tests primero → confirmar que FALLAN (red phase)
2. `PatientIdentityExtractor` antes que `CitaController` (US1)
3. Adapters de persistencia antes que controllers
4. Functional tests en verde como gate de completitud de la historia

### Parallel Opportunities

- **Phase 1**: T002, T003, T006, T007, T008, T009 en paralelo con T001/T004/T005
- **Phase 2**: T011–T016 en paralelo; T018 con T017; T020 con T019; T021–T025 en paralelo; T028–T030 con T027; T032–T034 con T031
- **US1**: T037, T040 en paralelo con sus pares; T046 con T045
- **US2**: T051, T053 en paralelo con T050, T052
- **US3**: T056, T060, T061 con sus pares; T058 con T057; T064 con T065
- **US3 puede ejecutarse en paralelo con US2** una vez Phase 2 completa

---

## Parallel Example: User Story 1

```bash
# Tests en paralelo (red phase — deben fallar):
Task T036: CitaTest.java
Task T037: FranjaHorariaTest.java  ← paralelo con T036

# Adapters en paralelo (tras T043 ReservarCitaService):
Task T044: CitaPersistenceAdapter.java
Task T045: FranjaHorariaPersistenceAdapter.java
Task T046: PacientePersistenceAdapter.java  ← paralelo con T044/T045
```

---

## Implementation Strategy

### MVP First (US1 only)

1. Completar Phase 1: Setup (T001–T009)
2. Completar Phase 2: Foundational (T010–T035) — CRÍTICO
3. Completar Phase 3: US1 (T036–T049)
4. **PARAR Y VALIDAR**: `./gradlew test --tests "*.ReservarCitaFunctionalTest*"` verde
5. Demo: `POST /api/v1/citas` con `X-Patient-Id` header funciona; `GET /actuator/health` UP

### Incremental Delivery

1. Phase 1 + 2 → Esqueleto CA compilando + tablas H2 con datos + Actuator UP
2. US1 → Reserva exitosa; validar independientemente
3. US2 → Doble reserva bloqueada; validar 409
4. US3 → Exploración de disponibilidad; demo flujo E-E completo
5. Polish → JaCoCo ≥ 80%; baseline p95 < 3s documentado

### Parallel Team Strategy (2 devs)

Tras completar Phase 2:
- **Dev A**: US1 (Phase 3) → US2 (Phase 4)
- **Dev B**: US3 (Phase 5) en paralelo

---

## Notes

- `[P]` = archivos distintos, sin dependencias bloqueantes — lanzar en paralelo
- `[Story]` = trazabilidad a User Story de spec.md
- Tests BDD DEBEN fallar antes de implementar (Constitution Principle II — no negociable)
- `PatientIdentityExtractor` usa `X-Patient-Id` header en MVP; producción → JWT claim `sub`
- `X-Idempotency-Key` removido del contrato (YAGNI — se añade cuando haya FR explícita)
- Clases en `org.ups.citasalud.openapi.*` excluidas de JaCoCo
- `ddl-auto: none`: el esquema lo controla `schema.sql`, no Hibernate — no cambiar
- Ver `quickstart.md` para validación manual y H2 console debugging

