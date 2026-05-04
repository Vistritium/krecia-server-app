#!/usr/bin/env bash
set -euo pipefail
trap 'status=$?; echo "ERROR: ${BASH_SOURCE[0]}:$LINENO: command failed with exit $status: $BASH_COMMAND" >&2' ERR

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/deploy-common.sh"

SERVICE_NAME="${SERVICE_NAME:-serverapp}"

: "${SSHPASS:?SSHPASS is required}"

require_deploy_tools
setup_known_host "$EXEC_HOST"

sshpass -e ssh \
  -p "$DEPLOY_PORT" \
  "$DEPLOY_USER@$EXEC_HOST" \
  "cd '$REMOTE_BASE_DIR' && sudo docker compose pull '$SERVICE_NAME' && sudo docker compose up --force-recreate --no-deps -d '$SERVICE_NAME'"
