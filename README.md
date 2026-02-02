# opc-datashare

Monorepo pour démonstration : frontend Angular + backend Spring Boot.

## Structure

- `frontend/` — Angular 16+ app
- `backend/` — Spring Boot 3.x (Java 17+)

## Démarrage rapide

### Frontend

```bash
cd frontend
npm install
npm start
```

> Le serveur de développement Angular sera disponible sur http://localhost:4200

### Backend

```bash
cd backend
mvn spring-boot:run
```

> L'API sera disponible sur http://localhost:8080 (ex: GET /api/hello)

---

N'hésitez pas à demander : je peux aussi lancer `npm install` ou `mvn package` pour vous, ou créer des workflows CI/CD.
