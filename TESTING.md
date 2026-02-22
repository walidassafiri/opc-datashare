# TESTING

## Objectif
Mettre en place un plan de tests simple et exécutable couvrant les fonctionnalités critiques:
- authentification (`register`, `login`)
- upload de fichier
- téléchargement de fichier

## Périmètre des tests

### 1. Tests unitaires backend
- `UploadServiceImplTest` 
- `UploadControllerTest` 
- `AuthServiceTest` 
- `UserServiceTest` 

### 2. Test d'intégration backend
- `AuthUploadDownloadIT` :
  - register
  - login
  - upload
  - info
  - download

### 3. Tests unitaires frontend (Angular)
- `auth.service.spec.ts`
- `auth.guard.spec.ts`
- `file.service.spec.ts`

### 4. Test end-to-end Cypress
- `frontend/cypress/e2e/critical-flow.cy.ts`
- `frontend/cypress/e2e/upload-delete.cy.ts`
- `frontend/cypress/e2e/download-wrong-password.cy.ts`
- `frontend/cypress/e2e/upload-forbidden-extension.cy.ts`
- Scénarios:
  - `register -> upload -> download`
  - `register -> upload -> delete`
  - `download avec mauvais mot de passe`
  - `upload extension interdite (.exe)`

## Couverture de code
- Couverture backend imposée dans `backend/pom.xml` via JaCoCo.
- Seuil bloquant: **80% minimum** (`LINE COVEREDRATIO >= 0.80`) au `mvn verify`.

## Commandes d'exécution

### 1) Backend (tests + couverture JaCoCo)

Option locale:
```bash
cd backend
mvn verify
```

Option Docker (recommandée si `mvn` absent):
```bash
# depuis la racine du repo
docker run --rm -v "$PWD/backend":/workspace -w /workspace maven:3.9.9-eclipse-temurin-17 mvn verify
```

### 2) Frontend unit tests (Angular/Karma)

Option locale:
```bash
cd frontend
npm install
npm run test:ci
```

Option Docker:
```bash
# depuis la racine du repo
docker run --rm -v "$PWD/frontend":/app -w /app cypress/browsers:node20.11.0-chrome121-ff122 bash -lc "npm install && npm run test:ci"
```

### 3) Cypress E2E (scénario critique)
Pré-requis: backend + db démarrés et frontend disponible sur `http://localhost:4200`.

Lancer backend + db (Docker Compose):
```bash
# depuis la racine du repo
docker compose up -d db backend
```

Lancer frontend (local):
```bash
cd frontend
npm install
npm start
```

Puis exécuter Cypress (local):
```bash
cd frontend
npm run e2e:run
```

Ou exécuter Cypress en Docker:
```bash
# depuis la racine du repo
docker run --rm -v "$PWD/frontend":/e2e -w /e2e cypress/included:13.17.0 cypress run --config baseUrl=http://host.docker.internal:4200
```

### 4) Vérifier les artefacts générés
```bash
# depuis la racine du repo
ls -la backend/target/site/jacoco || true
ls -la backend/target/surefire-reports || true
ls -la backend/target/failsafe-reports || true
ls -la frontend/coverage/frontend || true
```

## Résultats exploitables (emplacements)
- Backend unit/integration:
  - `backend/target/surefire-reports/`
  - `backend/target/failsafe-reports/`
- Couverture backend:
  - `backend/target/site/jacoco/index.html`
  - `backend/target/site/jacoco/jacoco.xml`
- Frontend couverture:
  - `frontend/coverage/frontend/`
- E2E:
  - sorties console Cypress + captures éventuelles
- Dossier central (manuel):
  - `reports/testing/`

## Critères d'acceptation
- Tous les tests backend passent.
- Tous les tests frontend passent.
- Le scénario Cypress critique passe.
- La couverture backend atteint au moins 80%.
