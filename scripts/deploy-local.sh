#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

BACKEND_ONLY="${1:-}"

if ! command -v docker >/dev/null 2>&1; then
  echo "[deploy-local] Docker is required."
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[deploy-local] Docker Compose plugin is required."
  exit 1
fi

echo "[deploy-local] Building and starting database and backend..."
docker compose up -d --build db backend

echo "[deploy-local] Services status:"
docker compose ps

echo

echo
echo "[deploy-local] Starting frontend..."
cd frontend
npm start
