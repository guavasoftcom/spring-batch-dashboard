#!/usr/bin/env bash
# Build the combined Spring Batch Dashboard Docker image locally.
#
# Mirrors what .github/workflows/release.yml does in CI: builds the SPA, bakes
# it into Spring Boot's classpath:/static/, repackages the JAR, and runs the
# root Dockerfile against the resulting artifact.
#
# Usage:
#   scripts/build-image.sh [TAG] [-- <extra docker build args>]
#
# Examples:
#   scripts/build-image.sh                              # tags as spring-batch-dashboard:local
#   scripts/build-image.sh spring-batch-dashboard:dev   # custom tag
#   scripts/build-image.sh -- --no-cache                # forward flags to docker build
#
# The SPA is built with an empty VITE_BACKEND_BASE_URL so it calls the API on its own
# origin, whatever host the image ends up served from. Only override that
# (VITE_BACKEND_BASE_URL=https://api.example.com scripts/build-image.sh) if you intend
# to point the bundle at a backend on a different origin — the value is baked in and
# the backend's CORS allow-list has to include the SPA's origin.

set -euo pipefail

# Always run from the repo root regardless of where the user invokes this from.
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/.." &>/dev/null && pwd)"
cd "${REPO_ROOT}"

TAG="${1:-spring-batch-dashboard:local}"
if [[ $# -gt 0 ]]; then shift; fi
if [[ "${1:-}" == "--" ]]; then shift; fi
EXTRA_DOCKER_ARGS=("$@")

log() { printf '\n\033[1;36m▸ %s\033[0m\n' "$*"; }

log "Building frontend (yarn install + yarn build)"
(
  cd frontend
  yarn install --immutable
  # Always export the var — an empty value makes the SPA issue same-origin relative
  # requests (correct for this image, where Spring Boot serves both the SPA and the
  # API) and, because process env beats .env files in Vite, it also stops a
  # developer's .env.local from baking http://localhost:8080 into the bundle.
  VITE_BACKEND_BASE_URL="${VITE_BACKEND_BASE_URL:-}" yarn build
)

log "Bundling SPA into Spring Boot static resources"
STATIC_DIR="backend/src/main/resources/static"
rm -rf "${STATIC_DIR}"
mkdir -p "${STATIC_DIR}"
cp -R frontend/dist/. "${STATIC_DIR}/"

log "Packaging backend JAR (skipping tests)"
(
  cd backend
  ./mvnw --batch-mode --no-transfer-progress -DskipTests package
)

log "Building Docker image: ${TAG}"
docker build -t "${TAG}" ${EXTRA_DOCKER_ARGS[@]+"${EXTRA_DOCKER_ARGS[@]}"} .

log "Done. Run with:  docker run --rm -p 8080:8080 ${TAG}"
