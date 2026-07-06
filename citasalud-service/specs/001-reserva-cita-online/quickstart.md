# Quickstart: Reserva de Cita en Línea 24/7

**Feature**: US-01 · 001-reserva-cita-online
**Date**: 2026-06-27

Esta guía describe cómo validar que la funcionalidad de reserva en línea está funcionando
correctamente de extremo a extremo.

---

## Prerrequisitos

- Java 25 instalado (`java -version`)
- Gradle Wrapper disponible (`./gradlew --version`)
- Puerto 8080 libre

---

## 0. Estructura DB (prerequisito)

El proyecto usa SQL init de Spring Boot. Antes de arrancar:

- `src/main/resources/db/schema.sql` — DDL de las 4 tablas (cargado automáticamente)
- `src/main/resources/db/data.sql` — datos pre-cargados: médicos, pacientes, franjas
- `src/test/resources/db/data-test.sql` — datos adicionales solo para tests

No es necesario ejecutar migraciones manualmente; Spring Boot los aplica al arrancar con
`spring.sql.init.mode: always` y `jpa.hibernate.ddl-auto: none`.

---

## 1. Compilar y ejecutar todas las pruebas

```bash
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

**Resultado esperado**:
- `BUILD SUCCESSFUL`
- Reporte JaCoCo generado en `build/reports/jacoco/test/html/index.html`
- Cobertura global ≥ 80% y por clase > 80%

---

## 2. Ejecutar la aplicación en modo desarrollo

```bash
./gradlew bootRun
```

La aplicación arranca en `http://localhost:8080`.

---

## 3. Validar Escenario US1-P1: Reserva exitosa fuera de horario

### 3.1 Identificación del paciente (FR-010 — stub de desarrollo)

En MVP/desarrollo, el `pacienteId` se pasa vía header `X-Patient-Id` (implementado en
`PatientIdentityExtractor`). En producción se reemplaza por la extracción del claim `sub`
del JWT Bearer — esa migración es una historia futura.

Usar el UUID del paciente insertado en `data.sql`:

```
X-Patient-Id: 00000000-0000-0000-0000-000000000001
```

### 3.2 Listar médicos disponibles

```bash
curl -s http://localhost:8080/api/v1/medicos \
  -H "Authorization: Bearer <token>" | jq .
```

**Resultado esperado**:
```json
{
  "medicos": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "nombre": "Ana",
      "apellido": "García López",
      "especialidad": "Medicina General"
    }
  ]
}
```

### 3.3 Consultar disponibilidad del médico

```bash
curl -s "http://localhost:8080/api/v1/medicos/550e8400-e29b-41d4-a716-446655440000/disponibilidad?fecha=2026-07-15" \
  -H "Authorization: Bearer <token>" | jq .
```

**Resultado esperado**: Al menos una franja con `"disponible": true`.

### 3.4 Reservar la franja disponible

```bash
curl -s -X POST http://localhost:8080/api/v1/citas \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"franjaHorariaId": "<id-franja-disponible>"}' | jq .
```

**Resultado esperado** (HTTP 201):
```json
{
  "citaId": "...",
  "estado": "CONFIRMADA",
  "medico": { ... },
  "fecha": "2026-07-15",
  "horaInicio": "09:00",
  "horaFin": "09:30",
  "fechaCreacion": "..."
}
```

**Verificación adicional**:
- La franja reservada ya no aparece como `disponible: true` al volver a consultar `/disponibilidad`.
- Los logs de la aplicación muestran el evento de notificación WhatsApp publicado.

---

## 4. Validar Escenario US2-P1: Franja ya ocupada

Intentar reservar la misma franja del paso 3.4:

```bash
curl -s -X POST http://localhost:8080/api/v1/citas \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"franjaHorariaId": "<id-franja-ya-reservada>"}' | jq .
```

**Resultado esperado** (HTTP 409):
```json
{
  "codigo": "FRANJA_OCUPADA",
  "mensaje": "La franja horaria seleccionada ya no está disponible. Por favor, elija otra franja.",
  "timestamp": "..."
}
```

---

## 5. Validar Escenario US3-P2: Sin disponibilidad en fecha

```bash
curl -s "http://localhost:8080/api/v1/medicos/550e8400-e29b-41d4-a716-446655440000/disponibilidad?fecha=2099-12-31" \
  -H "Authorization: Bearer <token>" | jq .
```

**Resultado esperado** (HTTP 200):
```json
{
  "medicoId": "550e8400-e29b-41d4-a716-446655440000",
  "fecha": "2099-12-31",
  "franjas": []
}
```

---

## 6. Verificar cobertura de pruebas

Abrir en el navegador:

```
build/reports/jacoco/test/html/index.html
```

Verificar:
- Cobertura global de instrucciones ≥ 80%
- Cada clase en `domain/` y `application/` con cobertura > 80%
- Clases generadas por openapi-generator excluidas del reporte

---

## Contratos y Modelo de Datos

- Contrato OpenAPI: [`contracts/openapi.yml`](./contracts/openapi.yml)
- Modelo de datos: [`data-model.md`](./data-model.md)
- Decisiones de diseño: [`research.md`](./research.md)
