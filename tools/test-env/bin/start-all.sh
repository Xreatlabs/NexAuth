#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
RUN_DIR="$REPO_ROOT/run"
JAVA_BIN="${JAVA_BIN:-/usr/lib/jvm/java-25-openjdk-amd64/bin/java}"

mkdir -p "$RUN_DIR/logs"

start_server() {
  local name=$1 dir=$2 log=$3; shift 3
  if [[ -f "$RUN_DIR/$name.pid" ]] && kill -0 "$(cat "$RUN_DIR/$name.pid")" 2>/dev/null; then
    echo "$name already running as PID $(cat "$RUN_DIR/$name.pid")"
    return 0
  fi
  # The subshell execs java, so the recorded PID is the JVM itself — not a
  # wrapper that exits and orphans the server (which breaks stop-all.sh).
  (cd "$dir" && exec nohup "$JAVA_BIN" "$@" > "$log" 2>&1) &
  echo $! > "$RUN_DIR/$name.pid"
  echo "Started $name as PID $(cat "$RUN_DIR/$name.pid")"
}

start_server paper "$RUN_DIR/paper" "$RUN_DIR/logs/paper.log" -Xms512M -Xmx2G -jar server.jar --nogui

sleep 8

start_server velocity "$RUN_DIR/velocity" "$RUN_DIR/logs/velocity.log" -Xms256M -Xmx1G -jar server.jar

echo "Logs:"
echo "  $RUN_DIR/logs/paper.log"
echo "  $RUN_DIR/logs/velocity.log"
