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
#   VITE_BACKEND_BASE_URL=https://dash.example.com scripts/build-image.sh
#   scripts/build-image.sh -- --no-cache                # forward flags to docker build

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
  if [[ -n "${VITE_BACKEND_BASE_URL:-}" ]]; then
    VITE_BACKEND_BASE_URL="${VITE_BACKEND_BASE_URL}" yarn build
  else
    yarn build
  fi
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
