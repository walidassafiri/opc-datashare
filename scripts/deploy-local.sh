#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "[deploy-local] Starting database and backend with Docker Compose..."
docker compose up -d db backend

echo "[deploy-local] Services status:"
docker compose ps

echo
echo "[deploy-local] Backend health check (may take a few seconds):"
if curl -fsS http://localhost:8065/actuator/health >/dev/null 2>&1; then
  echo "Backend is healthy"
else
  echo "Backend not ready yet. Retry in a few seconds: curl http://localhost:8065/actuator/health"
fi

echo
echo "[deploy-local] Start frontend in another terminal:"
echo "cd frontend && npm start"
