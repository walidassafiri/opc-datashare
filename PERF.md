# PERF

## Objectif
Mettre en place un test de performance sur un endpoint critique et analyser les métriques.

Endpoint testé:
- `POST /api/files/upload`

## Setup
Pré-requis:
- backend et base de données démarrés
- outils installés: `curl`, `k6`, `node`

Démarrage backend/db (depuis la racine):
```bash
docker compose up -d db backend
```

## Exécution du test
Depuis la racine:
```bash
./scripts/run-perf.sh
```

Option URL backend:
```bash
BACKEND_URL=http://localhost:8065 ./scripts/run-perf.sh
```

## Protocole
- Création d'un utilisateur de test.
- Login pour obtenir un JWT.
- Test `k6` (30s, 10 VUs) sur `POST /api/files/upload`.
- Récupération métriques Actuator (`http.server.requests`).

Seuils visés:
- `http_req_failed < 1%`
- `p95 http_req_duration < 800ms`

## Logs structurés
Le backend émet des logs JSON (un objet par ligne) via:
- `backend/src/main/resources/logback-spring.xml`

Exemple de champs:
- `timestamp`
- `level`
- `logger`
- `thread`
- `message`

## Métriques clés
Actuator est activé avec:
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/prometheus`

Métrique principale analysée:
- `http.server.requests` (global + filtre upload)

## Résultats générés
- `reports/perf/register-response.json`
- `reports/perf/login-response.json`
- `reports/perf/k6-summary.json`
- `reports/perf/k6-output.txt`
- `reports/perf/actuator-http-server-requests.json`
- `reports/perf/actuator-upload-metric.json`
- `reports/perf/summary.txt`

## Analyse des résultats
Lire `reports/perf/summary.txt`:
- `http_req_failed_rate`: stabilité
- `http_req_duration_p95`: latence de référence
- `http_req_duration_p99`: latence en queue
- `http_req_duration_avg`: moyenne
- `http_reqs_rate`: débit de requêtes

Interprétation rapide:
1. si `failed_rate > 0.01`, investiguer erreurs backend (logs JSON).
2. si `p95 > 800ms`, investiguer I/O disque ou DB.
3. comparer `actuator-upload-metric.json` avec `k6-summary.json` pour confirmer le comportement côté application.
