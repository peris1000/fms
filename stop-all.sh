#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

echo "Stopping microservices (dev compose)..."
docker compose -f docker-compose.dev.yml down --remove-orphans || true

echo "Stopping infrastructure (base compose)..."
docker compose -f docker-compose.yml down || true

echo "All containers stopped. Volumes preserved."
