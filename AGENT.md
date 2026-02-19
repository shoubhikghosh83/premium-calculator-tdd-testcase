# AGENT.md — TDD Test Repository

## Purpose

This repository defines the acceptance criteria and system behavior for the premium calculator modernization.

This repo drives development.

The modern implementation must satisfy these tests.

---

## Technology Stack

Backend API Tests:
- RestAssured
- JUnit 5
- Maven

Frontend UI Tests:
- Playwright

---

## STRICT TDD RULES

This repository must:

1. Define API behavior before implementation exists.
2. Generate failing tests first (Red Phase).
3. Represent true business requirements.
4. Act as contract tests for the backend.

---

## Test Requirements

Backend API tests must validate:

- Successful premium calculation
- Auto Insurance base premium
- Medical Insurance base premium
- House Insurance base premium
- 5% discount when name length > 10
- 10% increase when address contains "Metro"
- Order of operations: discount first, then increase
- Validation failures (null fields, invalid insurance type)

UI Playwright tests must validate:

- Form submission
- Display of calculated premium
- Error message rendering

---

## Constraints

- Do NOT implement backend logic here.
- Tests must fail initially.
- Tests must not mock backend logic.
- Tests must reflect real HTTP calls.

---

## Definition of Done (For Test Repo)

- All business rules are covered.
- Tests are deterministic.
- Tests fail before backend implementation.
- Tests are documented clearly.
