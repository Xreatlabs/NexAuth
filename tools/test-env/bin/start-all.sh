#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
RUN_DIR="$REPO_ROOT/run"
JAVA_BIN="${JAVA_BIN:-/usr/lib/jvm/java-25-openjdk-amd64/bin/java}"

mkdir -p "$RUN_DIR/logs"

if [[ -f "$RUN_DIR/paper.pid" ]] && kill -0 "$(cat "$RUN_DIR/paper.pid")" 2>/dev/null; then
  echo "Paper already running as PID $(cat "$RUN_DIR/paper.pid")"
else
  (cd "$RUN_DIR/paper" && nohup "$JAVA_BIN" -Xms512M -Xmx2G -jar server.jar --nogui > "$RUN_DIR/logs/paper.log" 2>&1 & echo $! > "$RUN_DIR/paper.pid")
  echo "Started Paper as PID $(cat "$RUN_DIR/paper.pid")"
fi

sleep 8

if [[ -f "$RUN_DIR/velocity.pid" ]] && kill -0 "$(cat "$RUN_DIR/velocity.pid")" 2>/dev/null; then
  echo "Velocity already running as PID $(cat "$RUN_DIR/velocity.pid")"
else
  (cd "$RUN_DIR/velocity" && nohup "$JAVA_BIN" -Xms256M -Xmx1G -jar server.jar > "$RUN_DIR/logs/velocity.log" 2>&1 & echo $! > "$RUN_DIR/velocity.pid")
  echo "Started Velocity as PID $(cat "$RUN_DIR/velocity.pid")"
fi

echo "Logs:"
echo "  $RUN_DIR/logs/paper.log"
echo "  $RUN_DIR/logs/velocity.log"
