<!--
SYNC IMPACT REPORT
==================
Version change: [TEMPLATE] → 1.0.0 (initial ratification)

Modified principles:
  - [PRINCIPLE_1_NAME] → I. Clean Architecture (Robert Martin)
  - [PRINCIPLE_2_NAME] → II. BDD Testing (NON-NEGOTIABLE)
  - [PRINCIPLE_3_NAME] → III. SOLID, YAGNI & DRY Practices
  - [PRINCIPLE_4_NAME] → IV. API First with OpenAPI
  - [PRINCIPLE_5_NAME] → V. Code Coverage & Quality Metrics

Added sections:
  - Quality Gates
  - Development Workflow

Removed sections:
  - [SECTION_2_NAME] / [SECTION_3_NAME] (generic placeholders replaced)

Templates requiring updates:
  ✅ .specify/templates/plan-template.md  — Constitution Check section now maps to the 5 principles
  ✅ .specify/templates/spec-template.md  — User Scenarios must use BDD Given/When/Then format
  ✅ .specify/templates/tasks-template.md — Test tasks are MANDATORY (not optional) per Principle II

Follow-up TODOs:
  - TODO(RATIFICATION_DATE): Confirm original project ratification date if different from 2026-06-27
-->

# CitaSalud Service Constitution

## Core Principles

### I. Clean Architecture (Robert Martin)

The service MUST be structured following Clean Architecture layers with strict dependency inversion:

- **Entities** (innermost): Domain objects and business rules. No framework or infrastructure dependencies allowed.
- **Use Cases**: Application-specific business logic. Depends only on Entities. MUST be independently testable.
- **Interface Adapters**: Controllers, presenters, gateways, and repository implementations. Translates data between
  Use Cases and the external world.
- **Frameworks & Drivers** (outermost): Spring Boot, JPA, HTTP clients, databases, message brokers.
  All framework coupling is confined here.

Dependency rule: source-code dependencies MUST point inward only. Outer layers depend on inner layers,
never the reverse. Cross-layer communication MUST use interfaces/ports defined in the inner layer.

No business logic is allowed in Controllers, Repositories, or any framework class.

### II. BDD Testing — Unit, Integration & Functional (NON-NEGOTIABLE)

All tests MUST be written using Behavior-Driven Development (BDD) conventions with Given/When/Then structure:

- **Unit tests**: Cover every Use Case, Entity, and Domain Service in isolation using mocks/stubs.
  MUST be written before or alongside implementation (test-first when possible).
- **Integration tests**: Verify interaction between layers (e.g., repository ↔ database, controller ↔ use case).
  MUST use a real or embedded database/broker; no mocking of infrastructure in integration scope.
- **Functional/acceptance tests**: Validate full end-to-end flows through the API against the OpenAPI contract.

All test scenarios MUST be expressed as:
```
Given [initial context]
When  [action performed]
Then  [expected outcome]
```

Test naming MUST reflect the scenario in plain language (e.g., `givenValidPatient_whenBookAppointment_thenReturns201`).
Tests are a deliverable, not optional — no feature is considered complete without passing tests at all three levels.

### III. SOLID, YAGNI & DRY Programming Practices

All production code MUST respect:

- **SOLID**:
  - *Single Responsibility*: Every class/method has one reason to change.
  - *Open/Closed*: Open for extension, closed for modification — use interfaces and polymorphism.
  - *Liskov Substitution*: Subtypes MUST be substitutable for their base types without altering correctness.
  - *Interface Segregation*: Clients MUST NOT be forced to depend on interfaces they do not use.
  - *Dependency Inversion*: Depend on abstractions, not concretions; inject dependencies.
- **YAGNI** (You Aren't Gonna Need It): Do not implement functionality until it is actually required.
  Speculative abstractions and premature generalization are PROHIBITED.
- **DRY** (Don't Repeat Yourself): Every piece of knowledge MUST have a single, authoritative representation.
  Duplication of logic (not just code) is a violation.

Code reviews MUST explicitly flag SOLID, YAGNI, and DRY violations as blocking.

### IV. API First with OpenAPI

Every API endpoint MUST be defined in an OpenAPI 3.x contract BEFORE implementation begins:

- The OpenAPI specification (`openapi.yml` at repository root or `src/main/resources/openapi/`) is the
  **source of truth** for all API surfaces.
- Code generation MUST use **openapi-generator** to produce server stubs and client DTOs from the contract.
  Hand-written controllers that duplicate contract definitions are PROHIBITED.
- The contract MUST be versioned alongside the codebase and kept in sync at all times.
  A contract drift check MUST be part of the CI pipeline.
- Breaking contract changes (removed fields, changed types, removed endpoints) MUST trigger a MAJOR version bump
  and MUST be communicated to all consumers before merge.
- Non-production internal APIs (e.g., test helpers) are exempt but MUST be documented in their own spec file.

### V. Code Coverage & Quality Metrics with JaCoCo

Coverage is a hard gate, not a guideline:

- **Per-class coverage**: MUST be > 80% (instruction + branch combined).
- **Global project coverage**: MUST be ≥ 80% (instruction coverage across all non-generated sources).
- **JaCoCo** is the mandatory tool for coverage measurement and reporting.
  Reports MUST be generated on every build (`mvn verify` / `gradle test jacocoTestReport`).
- Generated code (openapi-generator output, Lombok, MapStruct) MUST be excluded from coverage calculation.
- The build MUST FAIL if coverage thresholds are not met (`jacoco:check` goal enforced in CI).
- Coverage reports MUST be published as build artifacts so they are reviewable per pull request.

## Quality Gates

Every pull request MUST pass ALL of the following gates before merge:

1. **Architecture compliance**: No inward-to-outward dependency violations (ArchUnit or manual review).
2. **Test completeness**: Unit, integration, and functional tests exist and pass for the changed feature.
3. **Coverage thresholds**: JaCoCo reports show per-class > 80% and global ≥ 80%.
4. **Contract sync**: `openapi.yml` matches the implementation; openapi-generator output is up-to-date.
5. **SOLID/YAGNI/DRY review**: Code review sign-off confirming no principle violations.
6. **Build green**: `mvn verify` (or equivalent) passes with no skipped tests.

## Development Workflow

1. **Spec first**: Define the OpenAPI contract for the feature before writing any Java code.
2. **Generate stubs**: Run openapi-generator to produce server interfaces and request/response DTOs.
3. **Write tests (BDD)**: Author unit + integration + functional test scenarios (Given/When/Then).
   Confirm tests FAIL (red phase).
4. **Implement inside-out**: Start from Entities → Use Cases → Interface Adapters → Framework layer.
5. **Green phase**: Make all tests pass while honouring SOLID/YAGNI/DRY.
6. **Coverage check**: Run `mvn verify` and confirm JaCoCo thresholds pass.
7. **PR & gates**: Open pull request; all Quality Gates MUST pass before merge.

## Governance

- This Constitution supersedes all other practices, conventions, and informal agreements.
- Any amendment requires: (1) a written proposal describing the change and rationale,
  (2) review and approval by at least one senior team member, and (3) a migration plan for existing code.
- All pull requests and code reviews MUST explicitly verify compliance with each Core Principle.
- Complexity that violates a principle MUST be justified in the PR description and tracked in the
  Complexity Tracking section of the relevant `plan.md`.
- This document is reviewed every 3 months or after any major architectural shift.
- Versioning follows semantic versioning: MAJOR for principle removals/redefinitions,
  MINOR for new principles or material expansions, PATCH for clarifications and wording fixes.

**Version**: 1.0.0 | **Ratified**: 2026-06-27 | **Last Amended**: 2026-06-27
