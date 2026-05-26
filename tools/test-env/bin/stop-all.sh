#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
RUN_DIR="$REPO_ROOT/run"

for name in velocity paper; do
  pid_file="$RUN_DIR/$name.pid"
  if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
    pid="$(cat "$pid_file")"
    kill "$pid"
    echo "Stopped $name PID $pid"
  else
    echo "$name is not running from $pid_file"
  fi
  rm -f "$pid_file"
done
