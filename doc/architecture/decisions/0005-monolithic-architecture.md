# 5. Monolithic Architecture

Date: 2026-03-04

## Status

Accepted

## Context

Need to decide whether to build the Todo API as a single service or split into microservices (e.g., auth-service, todo-service, user-service).

## Decision

Build as a single Spring Boot monolith with clear internal layer separation (controller → service → repository).

## Alternatives Considered

### Microservices Architecture
**Pros:**
- Independently deployable services
- Might look good as a portfolio project

**Cons:**
- Massive overkill for todo app
- Increased complexity (service discovery, inter-service communication)
- Longer development time
- More expensive to host

## Consequences

### Positive
- Simple to develop, test, and deploy
- Single codebase, single deployment
- Easier debugging and monitoring
- Cost-effective (one Railway deployment)
- Clear layer separation allows future extraction if needed

### Negative
- Can't scale services independently (not needed at this scale)
- Single point of failure (acceptable for portfolio project)