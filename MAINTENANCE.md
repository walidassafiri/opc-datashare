# MAINTENANCE

## Objectif
Ce document décrit les procédures minimales pour maintenir et corriger l’application de façon claire et reproductible.

Périmètre:
- frontend Angular
- backend Spring Boot
- PostgreSQL via Docker Compose
- contexte local uniquement

## Maintenance courante (local)

### 1) Vérifier l’état des services
Depuis la racine du repo:
```bash
docker compose ps
```

### 2) Vérifier la santé du backend
```bash
curl -sS http://localhost:8065/actuator/health
```

### 3) Vérifier les métriques HTTP clés
```bash
curl -sS "http://localhost:8065/actuator/metrics/http.server.requests"
```

### 4) Vérifier les logs structurés backend (JSON)
Exemple (si backend lancé via Docker Compose):
```bash
docker compose logs backend --tail=100
```

### 5) Lancer les scans sécurité périodiques
```bash
./scripts/run-security.sh
```

### 6) Lancer un spot-check performance
```bash
./scripts/run-perf.sh
```

## Fréquence recommandée (local)
- Vérification `docker compose ps` et `health`: à chaque démarrage de session.
- Scans sécurité: avant chaque livraison/démonstration.
- Spot-check performance upload: avant chaque livraison ou après changement backend fichier.
- Nettoyage manuel des artefacts de test/coverage: en fin de cycle de développement.

## Procédure de correction (Incident -> Fix -> Validation)

### Incident
1. Identifier l’impact utilisateur (quels écrans/endpoints sont touchés).
2. Identifier l’endpoint concerné (ex: upload, download, auth).
3. Collecter l’erreur observée (logs + code HTTP + contexte).

### Fix
1. Reproduire le problème localement.
2. Appliquer un correctif minimal.
3. Documenter le changement (fichier touché + cause corrigée).

### Validation
1. Backend (tests + couverture):
```bash
cd backend
mvn verify
```
2. Frontend (tests unitaires):
```bash
cd frontend
npm run test:ci
```
3. E2E Cypress critique:
```bash
cd frontend
npm run e2e:run
```
4. Si dépendances touchées: relancer sécurité:
```bash
cd ..
./scripts/run-security.sh
```
5. Si endpoint critique touché: relancer perf:
```bash
./scripts/run-perf.sh
```

## Risques connus (local) et mesures
- **Risque**: saturation disque due au dossier `uploads/`.
  - **Mesure**: surveiller la taille et purger les fichiers non nécessaires.
- **Risque**: token JWT compromis en environnement local partagé.
  - **Mesure**: changer `JWT_SECRET`, limiter l'exposition de l'environnement local.
- **Risque**: dépendances vulnérables côté frontend.
  - **Mesure**: exécuter `./scripts/run-security.sh` régulièrement.
- **Risque**: régression fonctionnelle après correctif.
  - **Mesure**: exécuter systématiquement tests backend/frontend/e2e avant validation.

## Checklist pré-release
- tests backend OK
- couverture backend >= 80%
- tests frontend OK
- e2e critique OK
- scans sécurité disponibles (`reports/security/`)
- benchmark perf upload disponible (`reports/perf/`)
- docs à jour:
  - `TESTING.md`
  - `SECURITY.md`
  - `PERF.md`
  - `MAINTENANCE.md`

## Runbook de rollback minimal
1. Revenir au dernier commit/tag stable.
2. Redéployer le backend.
3. Vérifier:
   - `GET /actuator/health`
   - endpoint critique impacté (ex: upload)
4. Confirmer la résolution de l’incident.

## Interfaces / API / schéma
- Aucun changement d’API.
- Aucun changement de schéma ou type.
- Aucun impact runtime applicatif (documentation uniquement).

## Revue technique IA
- Le rapport de revue technique du code produit avec assistance IA est disponible dans:
  - `AI_CODE_REVIEW.md`
