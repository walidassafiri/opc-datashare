#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="$ROOT_DIR/reports/perf"
mkdir -p "$OUT_DIR"

BACKEND_URL="${BACKEND_URL:-http://localhost:8065}"
EMAIL="perf_$(date +%s)@example.com"
PASSWORD="password123"
AUTH_PAYLOAD="{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}"

for cmd in curl k6 node; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "$cmd is required" >&2
    exit 1
  fi
done

# Register can fail if email already exists, which is acceptable.
curl -sS -X POST "$BACKEND_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "$AUTH_PAYLOAD" > "$OUT_DIR/register-response.json" || true

LOGIN_RESPONSE="$(curl -sS -X POST "$BACKEND_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "$AUTH_PAYLOAD")"
echo "$LOGIN_RESPONSE" > "$OUT_DIR/login-response.json"

TOKEN="$(echo "$LOGIN_RESPONSE" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
if [ -z "$TOKEN" ]; then
  echo "Unable to retrieve JWT token from login response" >&2
  exit 1
fi

export K6_BASE_URL="$BACKEND_URL"
export K6_TOKEN="$TOKEN"
export K6_FILE_PATH="$ROOT_DIR/perf/sample-upload.txt"

k6 run "$ROOT_DIR/perf/k6-upload.js" \
  --summary-export "$OUT_DIR/k6-summary.json" \
  > "$OUT_DIR/k6-output.txt"

curl -sS "$BACKEND_URL/actuator/metrics/http.server.requests" > "$OUT_DIR/actuator-http-server-requests.json" || true
curl -sS "$BACKEND_URL/actuator/metrics/http.server.requests?tag=uri:%2Fapi%2Ffiles%2Fupload" > "$OUT_DIR/actuator-upload-metric.json" || true

node -e '
const fs = require("fs");
const outDir = process.argv[1];
const summaryPath = `${outDir}/k6-summary.json`;
const raw = JSON.parse(fs.readFileSync(summaryPath, "utf8"));

const m = raw.metrics || {};
// k6 summary format may expose values either directly at metric level
// or nested in a "values" object depending on version/config.
function metricValues(metric) {
  if (!metric) return {};
  if (metric.values && typeof metric.values === "object") return metric.values;
  return metric;
}

const reqDuration = metricValues(m.http_req_duration);
const reqFailed = metricValues(m.http_req_failed);
const reqRate = metricValues(m.http_reqs);

const lines = [
  `http_req_failed_rate=${reqFailed.rate ?? "n/a"}`,
  `http_req_duration_p95=${reqDuration["p(95)"] ?? "n/a"}`,
  `http_req_duration_p99=${reqDuration["p(99)"] ?? "n/a"}`,
  `http_req_duration_avg=${reqDuration.avg ?? "n/a"}`,
  `http_reqs_rate=${reqRate.rate ?? "n/a"}`,
];
fs.writeFileSync(`${outDir}/summary.txt`, lines.join("\n") + "\n", "utf8");
' "$OUT_DIR"

echo "Performance reports generated in $OUT_DIR"
