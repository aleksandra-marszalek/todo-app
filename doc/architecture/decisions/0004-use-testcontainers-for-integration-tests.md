# 4. Use Testcontainers for Integration Tests

Date: 2026-03-04

## Status

Accepted

## Context

Integration tests need a real database to test Spring Data JPA repositories, security filters, and full request-response cycles

## Decision

Use Testcontainers with MySQL 8 containers for all integration tests instead of in-memory databases like H2.

## Alternatives Considered

### H2 In-Memory Database
**Pros:**
- Very fast startup (milliseconds vs seconds)
- No Docker required
- Simpler setup

**Cons:**
- Different SQL dialect than production MySQL
- May hide production bugs (e.g., MySQL-specific constraints)
- Not true integration testing

### Shared Test Database
**Pros:**
- Closest to production setup
- No startup overhead

**Cons:**
- Test data can leak between runs
- Requires manual setup for developers
- Not isolated - tests can interfere with each other
- Doesn't work in CI/CD without extra setup

## Consequences

### Positive
- Tests run against real MySQL 8 (same as production)
- Catches MySQL-specific bugs that H2 would miss
- Complete isolation - each test suite gets fresh database
- No manual database setup for developers
- Demonstrates modern testing practices for portfolio
- Learning opportunity - Testcontainers is industry best practice

### Negative
- Slower test execution (5-10 seconds container startup)
- Requires Docker Desktop to be running
- More complex setup initially (took time to configure on Apple Silicon)
- Uses more system resources (RAM, CPU) during test runs