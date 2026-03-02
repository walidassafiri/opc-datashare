#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="$ROOT_DIR/dist/frontend"
REPORT_PATH="$ROOT_DIR/lighthouse.prod.report.html"

if [ ! -d "$DIST_DIR" ]; then
  echo "Build folder not found: $DIST_DIR" >&2
  echo "Run: npm run build:prod" >&2
  exit 1
fi

if ! command -v npx >/dev/null 2>&1; then
  echo "npx is required" >&2
  exit 1
fi

# Serve production build locally, run Lighthouse, then stop server.
npx -y http-server "$DIST_DIR" -p 4200 -c-1 >/tmp/opc-datashare-http.log 2>&1 &
SERVER_PID=$!
trap 'kill $SERVER_PID >/dev/null 2>&1 || true' EXIT

sleep 2
npx -y lighthouse http://localhost:4200 \
  --output html \
  --output-path "$REPORT_PATH" \
  --chrome-flags="--headless=new"

echo "Lighthouse report generated: $REPORT_PATH"
