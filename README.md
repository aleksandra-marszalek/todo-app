# Todo API

A Spring Boot REST API for managing todos with JWT authentication, built as part of my return to coding after a career break.

## Tech Stack
- Java 21
- Spring Boot 3.3.5
- MySQL 8
- Maven
- Spring Security + JWT
- Swagger/OpenAPI 3.0

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

## Running Locally

### 1. Start MySQL with Docker:
```bash
docker run --name todo-mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=tododb -p 3306:3306 -d mysql:8
```

### 2. Run the application:
```bash
./mvnw spring-boot:run
```

### 3. Access the API:
- API Base URL: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

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

## Using the API

### 1. Register a new user:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ola","email":"ola@example.com","password":"password123"}'
```

### 2. Login to get JWT token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ola","password":"password123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "ola",
  "email": "ola@example.com"
}
```

### 3. Use token to access protected endpoints:
```bash
curl -X GET http://localhost:8080/api/todos \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Or use Swagger UI:
1. Go to `http://localhost:8080/swagger-ui/index.html`
2. Click "Authorize" button (lock icon)
3. Paste your JWT token
4. Test all endpoints interactively!

## Architecture
```
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── model/          # JPA entities (User, Todo)
├── security/       # JWT utilities and filters
├── dto/            # Data transfer objects
├── exception/      # Global exception handler
└── config/         # Spring configuration
```

## Security Features
- Passwords hashed with BCrypt
- JWT tokens expire after 24 hours
- Stateless authentication (no sessions)
- Users can only access their own todos
- CORS configured for frontend integration

## What's Next
- 🔄 Add comprehensive test coverage
- 🔄 Deploy to Railway/Render
- 🔄 Frontend with React/Next.js

## Development Notes
Built as a learning project to get back into Java development after a career break. Demonstrates:
- Modern Spring Boot 3.x practices
- RESTful API design
- JWT authentication implementation
- Clean architecture with service layer
- Proper error handling and validation