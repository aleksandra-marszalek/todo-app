# 3. Use MySQL for Database

Date: 2026-03-04

## Status 

Accepted

## Context

The Todo API needs a relational database to store users and todos with relationships between them. There is a simple data model: users can have many todos, that can be managed by standard CRUD operations

## Decision

Use MySQL 8 as the primary database, accessed via Spring Data JPA and Hibernate.

## Alternatives Considered

### PostgreSQL
**Pros:**
- More advanced features (JSON types, full-text search)
- Better for complex queries

**Cons:**
- Overkill for simple todo app schema
- Less familiar to me personally
- No meaningful advantage for this use case

### MongoDB (NoSQL)
**Pros:**
- Schema flexibility
- Fast for simple reads
- Popular for modern apps

**Cons:**
- No built-in relationships (todos → users)
- Overkill for structured data
- JPA doesn't work well with NoSQL

## Consequences

### Positive
- Already familiar with MySQL from recent projects
- Well-supported by Spring Boot ecosystem
- Fast local development with Docker
- Free hosting options (eg. Railway)
- Mature, stable, and well-documented
- Perfect for relational data (users → todos)

### Negative
- Lacks some advanced PostgreSQL features (not needed for this project)
- Setup required for local development (Docker container already installed)