#!/usr/bin/env bash

die() {
  trap - ERR
  echo "ERROR: $*" >&2
  exit 1
}

DEPLOY_USER="${DEPLOY_USER:-root}"
DEPLOY_PORT="${DEPLOY_PORT:-8066}"

REMOTE_BASE_DIR="${REMOTE_BASE_DIR:-/home/kret/infra/kret-server}"
COPY_HOST="${COPY_HOST:-krecia.maciejnowicki.com}"
EXEC_HOST="${EXEC_HOST:-$COPY_HOST}"

require_deploy_tools() {
  if ! command -v sshpass >/dev/null; then
    die "sshpass is not installed"
  fi

  if ! command -v ssh-keyscan >/dev/null; then
    die "ssh-keyscan is not installed"
  fi
}

setup_known_host() {
  local host="$1"

  mkdir -p ~/.ssh
  chmod 700 ~/.ssh

  if ! ssh-keyscan -p "$DEPLOY_PORT" "$host" >> ~/.ssh/known_hosts; then
    die "Failed to scan SSH host key for $host:$DEPLOY_PORT"
  fi
}
