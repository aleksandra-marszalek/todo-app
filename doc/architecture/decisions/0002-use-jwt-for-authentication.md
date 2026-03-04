# 2: Use JWT for Authentication

Date: 2026-03-04

## Status

Accepted

## Context

The Todo API needs user authentication to secure endpoints and ensure users can only access their own todos. The application is a RESTful API that will be consumed by possibly a React frontend

## Decision
I will use JSON Web Tokens (JWT) for authentication with the following implementation:
- 24-hour token expiration
- HS256 signing algorithm
- Tokens include user ID and username
- Secret key stored in environment variables

## Alternatives Considered

### Session-based Authentication
**Pros:**
- Easy to revoke, built into Spring Security
- Smaller payload (just session ID)

**Cons:**
- Requires server-side session storage
- Harder to scale

### OAuth 2.0 / Social Login
**Pros:**
- No password management
- Users already trust providers

**Cons:**
- Much more complex to implement
- Dependency on third-party services
- Overkill for a simple todo app

## Consequences

### Positive
- Stateless architecture - easy to scale horizontally
- No need for session storage (Redis, database)
- Frontend-friendly - simple to store and send with requests
- Perfect for REST APIs and Single Page Apps
- Industry-standard approach

### Negative
- Cannot revoke tokens before expiration (24-hour window if token is compromised is implemented to mitigate)
- Slightly larger payload compared to session IDs (Not really an issue at the moment)
- User data in token can become stale (if user details change - again, not significant enough)
- Requires careful secret key management (stored in env variable)