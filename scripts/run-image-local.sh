#!/usr/bin/env bash
# Run the combined Spring Batch Dashboard image with the `local` Spring profile
# active, wired to the docker-compose Postgres/MySQL/Oracle containers running
# on the host.
#
# Prereqs:
#   - backend/.env populated (OAuth client + DB creds — see backend/.env.example)
#   - DB stack already running on the host:
#       (cd backend && docker compose up -d)
#   - Image already built:
#       scripts/build-image.sh
#
# Usage:
#   scripts/run-image-local.sh [TAG]
#
# Then open http://localhost:8080.

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." &>/dev/null && pwd)"
cd "${REPO_ROOT}"

TAG="${1:-spring-batch-dashboard:local}"

if [[ ! -f backend/.env ]]; then
  echo "error: backend/.env not found. Copy backend/.env.example and fill it in first." >&2
  exit 1
fi

if ! docker image inspect "${TAG}" &>/dev/null; then
  echo "error: image '${TAG}' not found locally. Run scripts/build-image.sh first." >&2
  exit 1
fi

# Disable spring-boot-docker-compose lifecycle (no docker socket inside the
# container), point the datasource hostname placeholders at the host gateway,
# and rewrite the OAuth2 success URL + CORS origin to :8080 since the combined
# image serves the SPA from the same port as the API.
exec docker run --rm -p 8080:8080 \
  --env-file backend/.env \
  -e SPRING_PROFILES_ACTIVE=local \
  -e SPRING_DOCKER_COMPOSE_ENABLED=false \
  -e POSTGRES_HOST=host.docker.internal \
  -e MYSQL_HOST=host.docker.internal \
  -e ORACLE_HOST=host.docker.internal \
  -e APP_OAUTH2_SUCCESS_URL=http://localhost:8080/overview \
  -e APP_CORS_ALLOWED_ORIGINS=http://localhost:8080 \
  --add-host=host.docker.internal:host-gateway \
  "${TAG}"
