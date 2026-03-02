# OPC DataShare

Application web de partage de fichiers sécurisé:
- frontend Angular
- backend Spring Boot
- base PostgreSQL
- stockage local des fichiers (`uploads/`)

## 1) Prérequis

- Docker + Docker Compose
- Node.js LTS + npm
- Java 17 + Maven (optionnel si backend lancé via Docker)

## 2) Installation

```bash
git clone <repo-url>
cd opc-datashare
cd frontend && npm install && cd ..
```

## 3) Lancement

### Option recommandée (Docker pour DB + backend)

```bash
docker compose up -d db backend
cd frontend
npm start
```

- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8065`

### Option backend local (sans Docker backend)

```bash
docker compose up -d db
cd backend
mvn spring-boot:run
```

## 4) Utilisation

Parcours principal:
1. Créer un compte (`register`) ou se connecter (`login`).
2. Uploader un fichier (avec expiration, tags, mot de passe optionnel).
3. Consulter l'historique.
4. Télécharger via token.
5. Supprimer un fichier.

## 5) Structure du repository

- `frontend/`: application Angular
- `backend/`: API Spring Boot
- `swagger/openapi.yaml`: contrat API
- `scripts/`: scripts d'exécution locale (tests, sécurité, performance)
- `reports/`: rapports générés (testing/security/perf)

## 6) Qualité, sécurité, performance, maintenance

Documentation dédiée:
- `TESTING.md`
- `SECURITY.md`
- `PERF.md`
- `MAINTENANCE.md`
- `DOCUMENTATION_TECHNIQUE.md`
- `AI_CODE_REVIEW.md` (revue technique du code produit avec l'IA)

## 7) Scripts utiles

Depuis la racine du repo:

```bash
./scripts/deploy-local.sh     # démarrage local db + backend
./scripts/run-security.sh     # génération rapports sécurité
./scripts/run-perf.sh         # test perf upload + rapports
```

## 8) API

Spécification OpenAPI:
- `swagger/openapi.yaml`

Endpoints principaux:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/files/upload`
- `GET /api/files/history`
- `GET /api/files/download/{token}`
- `DELETE /api/files/{token}`

