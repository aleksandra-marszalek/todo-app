# Todo API

A Spring Boot REST API for managing todos with JWT authentication, built as part of my return to coding after a career break.

## 🚀 Live Demo

**Production API:** https://todo-app-production-218b.up.railway.app  
**Swagger Documentation:** https://todo-app-production-218b.up.railway.app/swagger-ui/index.html

Try it out:
```bash
# Register a new user
curl -X POST https://todo-app-production-218b.up.railway.app/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","email":"demo@example.com","password":"password123"}'

# Login
curl -X POST https://todo-app-production-218b.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password123"}'
```

## Tech Stack
- Java 21
- Spring Boot 3.3.5
- MySQL 8
- Maven
- Spring Security + JWT
- Swagger/OpenAPI 3.0
- TestContainers (for integration tests)

## Features
- ✅ Complete CRUD operations (Create, Read, Update, Delete)
- ✅ JWT Authentication (register/login)
- ✅ User-specific todos (users only see their own)
- ✅ Password encryption with BCrypt
- ✅ Input validation with custom error messages
- ✅ Global exception handling
- ✅ Service layer architecture
- ✅ RESTful API design
- ✅ Interactive API documentation with Swagger
- ✅ **Comprehensive test coverage (80%+) with Testcontainers**
- ✅ **Deployed to production on Railway**

## Prerequisites

Before running this project locally, ensure you have:

- **Java 21** -
```bash
  # Verify installation
  java -version  # Should show version 21.x.x
```

- **Maven 3.9+** - Usually bundled with IntelliJ, or use the included Maven wrapper
```bash
  # Verify installation (if not using wrapper)
  mvn -version
```

- **Docker Desktop** - [Download here](https://www.docker.com/products/docker-desktop/)
    - Required for running MySQL locally
    - Required for running tests (Testcontainers)
```bash
  # Verify Docker is running
  docker ps
```

## Running Locally

### 1. Clone the repository
```bash
git clone https://github.com/aleksandra-marszalek/todo-app.git
cd todo-api
```

### 2. Start MySQL with Docker
```bash
docker run --name todo-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=tododb \
  -p 3306:3306 \
  -d mysql:8
```

**Note:** If port 3306 is already in use:
```bash
# Stop existing MySQL container
docker stop todo-mysql && docker rm todo-mysql

# Or use a different port
docker run --name todo-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=tododb \
  -p 3307:3306 \
  -d mysql:8

# Then update application.properties to use port 3307
```

### 3. Run the application

**Option A: Using Maven wrapper (recommended)**
```bash
./mvnw spring-boot:run
```

**Option B: Using IntelliJ IDEA**
1. Open project in IntelliJ
2. Wait for Maven dependencies to download
3. Right-click `TodoApplication.java`
4. Select "Run 'TodoApplication'"

**Option C: Build and run JAR**
```bash
./mvnw clean package -DskipTests
java -jar target/todo-0.0.1-SNAPSHOT.jar
```

### 4. Access the application
- **API Base URL:** `http://localhost:8080/api`
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **Health Check:** `http://localhost:8080/api/health`

## Testing

### Prerequisites for Testing
- **Docker Desktop must be running** - Testcontainers needs Docker to spin up MySQL containers for integration tests

### Running Tests

**Run all tests:**
```bash
./mvnw test
```

**Run specific test class:**
```bash
./mvnw test -Dtest=TodoServiceTest
./mvnw test -Dtest=TodoControllerIntegrationTest
./mvnw test -Dtest=AuthControllerIntegrationTest
```

**Run tests with coverage report:**
```bash
./mvnw test jacoco:report
# View report at: target/site/jacoco/index.html
```

### Test Coverage

The project includes comprehensive test coverage:
- **Unit tests** - Service layer with Mockito (8 tests)
- **Integration tests** - Controllers with Testcontainers and real MySQL (12+ tests)
- **Authentication tests** - Registration and login flows (10+ tests)
- **Total:** 30+ tests covering happy paths and edge cases
- **Coverage:** 80%+ on service and controller layers

### Troubleshooting Tests

**If tests fail with Docker connection errors:**
1. Ensure Docker Desktop is running: `docker ps`
2. Check Docker socket permissions (Mac): Add `"min-api-version": "1.24"` to Docker Desktop settings → Docker Engine
3. Try restarting Docker Desktop

**If tests fail with port conflicts:**
- Testcontainers automatically assigns random ports, so this shouldn't happen
- If it does, ensure no other test runs are happening simultaneously

## API Endpoints

### Authentication (Public - No Token Required)
- `POST /api/auth/register` - Register a new user
```json
  {
    "username": "ola",
    "email": "ola@example.com",
    "password": "password123"
  }
```
- `POST /api/auth/login` - Login and receive JWT token
```json
  {
    "username": "ola",
    "password": "password123"
  }
```

### Todos (Protected - Requires JWT Token)
All todo endpoints require `Authorization: Bearer <token>` header.

- `GET /api/todos` - Get all todos for authenticated user
- `GET /api/todos/{id}` - Get specific todo by ID
- `POST /api/todos` - Create a new todo
```json
  {
    "title": "Buy groceries",
    "description": "Milk, eggs, bread",
    "completed": false
  }
```
- `PUT /api/todos/{id}` - Update a todo
- `DELETE /api/todos/{id}` - Delete a todo

### Health Check (Public)
- `GET /api/health` - Health check endpoint

### Using the API

**1. Register and get token:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ola","email":"ola@example.com","password":"password123"}'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "ola",
  "email": "ola@example.com"
}
```

**2. Use token to create a todo:**
```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{"title":"Buy milk","completed":false}'
```

**Or use Swagger UI for interactive testing!**

## Configuration

### Local Development (application.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tododb
spring.datasource.username=root
spring.datasource.password=password
```

## Deployment

**Platform:** Railway  
**Database:** Railway MySQL 8  
**CI/CD:** Automated deployment from GitHub main branch  
**Environment:** Production-ready with environment-based configuration

## Architecture
```
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access (Spring Data JPA)
├── model/          # JPA entities (User, Todo)
├── security/       # JWT utilities, filters, and Spring Security config
├── dto/            # Data transfer objects
├── exception/      # Global exception handler
├── config/         # Spring configuration (CORS, OpenAPI)
└── util/           # Utility classes (SecurityUtil)
```

## Security Features
- Passwords hashed with BCrypt (12 rounds)
- JWT tokens expire after 24 hours
- Stateless authentication (no sessions)
- Users can only access their own todos
- CORS configured for cross-origin requests

## Common Issues

**Port 3306 already in use:**
```bash
# Find and stop the conflicting process
lsof -i :3306
kill -9 <PID>

# Or use a different port (see "Running Locally" section)
```

**Tests failing - Docker not found:**
- Ensure Docker Desktop is running
- Check Docker socket: `ls -la /var/run/docker.sock`

## What I Learned

This project helped me get back into modern Java development after a career break:
- **Testing best practices** with Testcontainers and proper test isolation
- **Spring Security 6** with JWT authentication
- **Modern Java features** (Java 21, `var`, records-ready architecture)
- **Production deployment** with Railway and environment-based configuration
- **Clean architecture** with proper separation of concerns
- **API documentation** with OpenAPI/Swagger
