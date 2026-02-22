#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="$ROOT_DIR/reports/security"
mkdir -p "$OUT_DIR"

DATE_UTC="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo "$DATE_UTC" > "$OUT_DIR/scan-date.txt"

npm_ok=0
trivy_ok=0

if command -v npm >/dev/null 2>&1; then
  set +e
  (
    cd "$ROOT_DIR/frontend"
    npm audit --json --omit=dev > "$OUT_DIR/npm-audit.json"
  )
  npm_status=$?
  set -e

  # npm audit returns non-zero when vulnerabilities are found, so JSON presence is the real success condition.
  if [ -s "$OUT_DIR/npm-audit.json" ]; then
    npm_ok=1
  fi
else
  echo '{"error":"npm_not_found"}' > "$OUT_DIR/npm-audit.json"
fi

if command -v trivy >/dev/null 2>&1; then
  set +e
  trivy fs --format json --output "$OUT_DIR/trivy-fs.json" "$ROOT_DIR"
  trivy_status=$?
  set -e
  if [ -s "$OUT_DIR/trivy-fs.json" ]; then
    trivy_ok=1
  fi
elif command -v docker >/dev/null 2>&1; then
  set +e
  docker run --rm \
    -v "$ROOT_DIR":/src \
    aquasec/trivy:0.58.1 \
    fs --format json --output /src/reports/security/trivy-fs.json /src
  trivy_status=$?
  set -e
  if [ -s "$OUT_DIR/trivy-fs.json" ]; then
    trivy_ok=1
  fi
else
  echo '{"error":"trivy_not_found"}' > "$OUT_DIR/trivy-fs.json"
fi

if command -v node >/dev/null 2>&1; then
  node -e '
const fs = require("fs");
const path = require("path");
const outDir = process.argv[1];

function write(name, text) {
  fs.writeFileSync(path.join(outDir, name), text + "\n", "utf8");
}

// npm audit summary
try {
  const npmRaw = JSON.parse(fs.readFileSync(path.join(outDir, "npm-audit.json"), "utf8"));
  const v = (npmRaw.metadata && npmRaw.metadata.vulnerabilities) || {};
  const lines = [
    `critical=${v.critical || 0}`,
    `high=${v.high || 0}`,
    `moderate=${v.moderate || 0}`,
    `low=${v.low || 0}`,
    `info=${v.info || 0}`,
    `total=${v.total || 0}`
  ];
  write("npm-audit-summary.txt", lines.join("\n"));
} catch {
  write("npm-audit-summary.txt", "error=unable_to_parse_npm_audit");
}

// trivy summary
try {
  const trivyRaw = JSON.parse(fs.readFileSync(path.join(outDir, "trivy-fs.json"), "utf8"));
  const counts = { CRITICAL: 0, HIGH: 0, MEDIUM: 0, LOW: 0, UNKNOWN: 0 };
  let findings = 0;
  for (const r of (trivyRaw.Results || [])) {
    for (const v of (r.Vulnerabilities || [])) {
      findings += 1;
      const sev = (v.Severity || "UNKNOWN").toUpperCase();
      counts[sev] = (counts[sev] || 0) + 1;
    }
    findings += (r.Misconfigurations || []).length;
    findings += (r.Secrets || []).length;
  }
  const lines = [
    `critical=${counts.CRITICAL || 0}`,
    `high=${counts.HIGH || 0}`,
    `medium=${counts.MEDIUM || 0}`,
    `low=${counts.LOW || 0}`,
    `unknown=${counts.UNKNOWN || 0}`,
    `total_findings=${findings}`
  ];
  write("trivy-summary.txt", lines.join("\n"));
} catch {
  write("trivy-summary.txt", "error=unable_to_parse_trivy");
}
' "$OUT_DIR"
fi

cat > "$OUT_DIR/summary.txt" <<EOF
Security scan date (UTC): $DATE_UTC
Reports:
- reports/security/npm-audit.json
- reports/security/npm-audit-summary.txt
- reports/security/trivy-fs.json
- reports/security/trivy-summary.txt
EOF

if [ "$npm_ok" -ne 1 ] || [ "$trivy_ok" -ne 1 ]; then
  echo "Security scan incomplete. Check reports/security/*.json and summary files." >&2
  exit 1
fi

echo "Security reports generated in $OUT_DIR"
