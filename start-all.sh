#!/usr/bin/env bash
set -euo pipefail

# Orchestrates infra + all three microservices using Docker (dev profiles)
# Minimal changes approach: reuse existing docker-compose.yml for infra
# and start services in Maven dev mode inside lightweight containers.

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

bold() { echo -e "\e[1m$*\e[0m"; }

bold "[1/5] Build shared DTOs (skip tests)"
mvn -q -f shared-dtos/pom.xml clean install -DskipTests || true

bold "[2/5] Start infrastructure (Redpanda, MySQL, Keycloak, Jaeger)"
docker compose -f docker-compose.yml up -d

bold "[3/5] Wait a bit for infra to initialize..."
sleep 15

# Optional quick sanity pings (non-fatal)
if command -v curl >/dev/null 2>&1; then
  curl -fsS http://localhost:18080/health/ready >/dev/null 2>&1 && echo "Keycloak is responding" || echo "Keycloak not yet responding (continuing)"
  curl -fsS http://localhost:16686 >/dev/null 2>&1 && echo "Jaeger UI up" || true
fi

echo "MySQL should be on localhost:13306, Redpanda on localhost:19092"

bold "[4/5] Start microservices (A, B, C) in dev mode containers"
docker compose -f docker-compose.dev.yml up --remove-orphans

# Note: The above is foreground (shows logs). Use -d if you want detached mode:
# docker compose -f docker-compose.dev.yml up -d

bold "[5/5] All services launched"
echo "Health endpoints:"
echo "- A: http://localhost:8083/q/health"
echo "- B: http://localhost:8084/q/health"
echo "- C: http://localhost:8085/q/health"

echo
bold "To stop everything:" 
cat <<EOF
./stop-all.sh
# or in another shell: docker compose -f docker-compose.dev.yml down && docker compose -f docker-compose.yml down
EOF
