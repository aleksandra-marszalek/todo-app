
# Todo API

A Spring Boot REST API for managing todos, built as part of my return to coding after a career break.

## Tech Stack
- Java 21
- Spring Boot 4.0.3
- MySQL 8
- Maven

## Features
- ✅ Create todos
- ✅ List all todos
- ✅ Get todo by ID
- 🔄 Update todos (coming soon)
- 🔄 Delete todos (coming soon)

## Running Locally

1. Start MySQL with Docker:
```bash
docker run --name todo-mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=tododb -p 3306:3306 -d mysql:8
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

3. API will be available at `http://localhost:8080/api`

## API Endpoints

- `GET /api/health` - Health check
- `GET /api/todos` - Get all todos
- `POST /api/todos` - Create a new todo
- `GET /api/todos/{id}` - Get todo by ID

## What's Next
- Adding authentication (JWT)
- Frontend with React
- Deploy to Railway/Render
