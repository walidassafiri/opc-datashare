# Backend (Spring Boot)

Minimal Spring Boot application.

Commands:

- Run in dev: `mvn spring-boot:run`
- Build jar: `mvn package` then `java -jar target/backend-0.0.1-SNAPSHOT.jar`

The app exposes a sample endpoint: `GET /api/hello`.

Authentication (MVP):

- POST `/api/auth/register`  -> body: `{ "email": "user@example.com", "password": "strongpass" }` returns `{ "token": "..." }`
- POST `/api/auth/login`     -> body: `{ "email": "user@example.com", "password": "strongpass" }` returns `{ "token": "..." }`
- GET `/api/me` (protected) -> requires header `Authorization: Bearer <token>`, returns `{ "email": "...", "id": ... }`

Quick test with curl (after server running):

1) Register

```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"email":"test@example.com","password":"password123"}'
```

2) Login

```bash
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"test@example.com","password":"password123"}'
```

3) Use token

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/me
```

Note: set a secure `jwt.secret` in `backend/src/main/resources/application.properties` or via environment variables (e.g., `JWT_SECRET`) before running in production.

