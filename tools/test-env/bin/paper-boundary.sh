#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
RUN_DIR="$REPO_ROOT/run"
CACHE="$RUN_DIR/cache/paper"
DATA_CACHE="$RUN_DIR/cache/nexauth-paper-data"
WORKBASE="$RUN_DIR/paper-only"
BOT_SOURCE="$REPO_ROOT/tools/test-env/bot"
BOTDIR="$RUN_DIR/bot"
NEXAUTH="$REPO_ROOT/Plugin/build/libs/NexAuth.jar"
VIA_VERSION_JAR="$(find "$RUN_DIR/paper/plugins" -maxdepth 1 -name 'ViaVersion-*.jar' | sort | tail -n 1)"
VIA_BACKWARDS_JAR="$(find "$RUN_DIR/paper/plugins" -maxdepth 1 -name 'ViaBackwards-*.jar' | sort | tail -n 1)"
JAVA21=${JAVA21:-/usr/lib/jvm/java-21-openjdk-amd64/bin/java}
JAVA25=${JAVA25:-/usr/lib/jvm/java-25-openjdk-amd64/bin/java}

if [[ -n "${VERSIONS:-}" ]]; then
  read -r -a versions <<< "$VERSIONS"
else
  versions=(1.20 1.20.2 1.20.4 1.20.6 1.21 1.21.3 1.21.4 1.21.5 1.21.6 1.21.8 1.21.10 1.21.11 26.1.2)
fi

mkdir -p "$CACHE" "$WORKBASE" "$DATA_CACHE"
results="$WORKBASE/boundary-results.tsv"
: > "$results"
failed=0
SERVER_PID=""
SERVER_PGID=""

paper_url() {
  local version="$1"
  curl -fsSL "https://fill.papermc.io/v3/projects/paper/versions/$version/builds" |
    node -e "const fs=require('fs'); const builds=JSON.parse(fs.readFileSync(0,'utf8')); const stable=builds.find(b=>b.channel==='STABLE')||builds[0]; if(!stable) process.exit(2); console.log(stable.downloads['server:default'].url);"
}

bot_version() {
  case "$1" in
    26.1.2) printf '1.21.11' ;;
    *) printf '%s' "$1" ;;
  esac
}

java_for() {
  case "$1" in
    26.*) printf '%s' "$JAVA25" ;;
    *) printf '%s' "$JAVA21" ;;
  esac
}

start_server() {
  local dir="$1" java_bin="$2" log="$3"
  SERVER_PID=""
  SERVER_PGID=""
  pushd "$dir" >/dev/null
  setsid --wait bash -c 'tail -f /dev/null | "$1" -jar server.jar --nogui' _ "$java_bin" > "$log" 2>&1 &
  SERVER_PID=$!
  popd >/dev/null
  sleep 0.2
  SERVER_PGID=$(ps -o pgid= -p "$SERVER_PID" 2>/dev/null | tr -d ' ' || true)
}

stop_server() {
  local pid="${SERVER_PID:-}" pgid="${SERVER_PGID:-}"
  [[ -z "$pid" ]] && return 0
  if [[ -n "$pgid" ]]; then kill -TERM -"$pgid" 2>/dev/null || true; else kill "$pid" 2>/dev/null || true; fi
  for _ in $(seq 1 25); do
    if ! kill -0 "$pid" 2>/dev/null; then SERVER_PID=""; SERVER_PGID=""; return 0; fi
    sleep 1
  done
  if [[ -n "$pgid" ]]; then kill -KILL -"$pgid" 2>/dev/null || true; else kill -9 "$pid" 2>/dev/null || true; fi
  SERVER_PID=""
  SERVER_PGID=""
}

wait_log() {
  local pid="$1" log="$2" pattern="$3" seconds="$4"
  for _ in $(seq 1 "$seconds"); do
    if rg -q "$pattern" "$log" 2>/dev/null; then return 0; fi
    if ! kill -0 "$pid" 2>/dev/null; then return 1; fi
    sleep 1
  done
  return 1
}

wait_log_and_port() {
  local pid="$1" log="$2" pattern="$3" seconds="$4"
  for _ in $(seq 1 "$seconds"); do
    if rg -q "$pattern" "$log" 2>/dev/null && timeout 1 bash -c '</dev/tcp/127.0.0.1/25565' 2>/dev/null; then return 0; fi
    if ! kill -0 "$pid" 2>/dev/null; then return 1; fi
    sleep 1
  done
  return 1
}

trap stop_server EXIT

if [[ ! -s "$NEXAUTH" ]]; then
  (cd "$REPO_ROOT" && JAVA_HOME="$JAVA21" ./gradlew shadowJar)
fi

mkdir -p "$BOTDIR"
cp "$BOT_SOURCE/"*.js "$BOTDIR/"
cp "$BOT_SOURCE/package.json" "$BOTDIR/package.json"

if [[ ! -d "$BOTDIR/node_modules" ]]; then
  (cd "$BOTDIR" && npm install)
fi

for version in "${versions[@]}"; do
  bver="$(bot_version "$version")"
  server_dir="$WORKBASE/$version"
  jar="$CACHE/paper-$version.jar"
  echo "=== Paper $version / bot $bver ==="

  if [[ ! -s "$jar" ]]; then
    curl -fL "$(paper_url "$version")" -o "$jar"
  fi

  rm -rf "$server_dir"
  mkdir -p "$server_dir/plugins"
  cp "$jar" "$server_dir/server.jar"
  cp "$NEXAUTH" "$server_dir/plugins/NexAuth.jar"
  cp "$VIA_VERSION_JAR" "$server_dir/plugins/"
  cp "$VIA_BACKWARDS_JAR" "$server_dir/plugins/"
  [[ -d "$DATA_CACHE/NexAuth" ]] && cp -a "$DATA_CACHE/NexAuth" "$server_dir/plugins/"

  printf 'eula=true\n' > "$server_dir/eula.txt"
  printf '%s\n' \
    'server-port=25565' \
    'online-mode=false' \
    'enforce-secure-profile=false' \
    'motd=NexAuth Paper boundary matrix' \
    'spawn-protection=0' \
    'view-distance=2' \
    'simulation-distance=2' \
    > "$server_dir/server.properties"

  java_bin="$(java_for "$version")"

  if [[ ! -d "$server_dir/plugins/NexAuth" ]]; then
    first_log="$server_dir/first-start.log"
    start_server "$server_dir" "$java_bin" "$first_log"
    if ! wait_log "$SERVER_PID" "$first_log" 'new configuration was generated|Done \(' 360; then
      echo "FIRST_START_FAIL $version"
      tail -n 160 "$first_log" || true
      printf '%s\t%s\tFIRST_START_FAIL\n' "$version" "$bver" >> "$results"
      failed=1
      stop_server
      continue
    fi
    if rg -q 'IllegalAccessError|CorruptedFrameException|larger than expected|too small|\[.*ERROR\]' "$first_log"; then
      echo "FIRST_LOG_ERROR $version"
      rg -n 'IllegalAccessError|CorruptedFrameException|larger than expected|too small|\[.*ERROR\]' "$first_log" || true
      printf '%s\t%s\tFIRST_LOG_ERROR\n' "$version" "$bver" >> "$results"
      failed=1
      stop_server
      continue
    fi
    stop_server
  fi

  log="$server_dir/server.log"
  start_server "$server_dir" "$java_bin" "$log"
  if ! wait_log_and_port "$SERVER_PID" "$log" 'Inventory hiding feature enabled|Done \(' 360; then
    echo "SERVER_FAIL $version"
    tail -n 200 "$log" || true
    printf '%s\t%s\tSERVER_FAIL\n' "$version" "$bver" >> "$results"
    failed=1
  else
    rm -rf "$DATA_CACHE/NexAuth"
    cp -a "$server_dir/plugins/NexAuth" "$DATA_CACHE/NexAuth" || true
    name="B$(printf '%s' "$version" | tr -cd '0-9')$(date +%S)"
    if (cd "$BOTDIR" && MC_PORT=25565 MC_TEST_TIMEOUT=60000 node smoke-paper.js "$bver" "$name"); then
      if rg -q 'IllegalAccessError|CorruptedFrameException|larger than expected|too small|\[.*ERROR\]' "$log"; then
        echo "LOG_ERROR $version"
        rg -n 'IllegalAccessError|CorruptedFrameException|larger than expected|too small|\[.*ERROR\]' "$log" || true
        printf '%s\t%s\tLOG_ERROR\n' "$version" "$bver" >> "$results"
        failed=1
      else
        echo "PASS $version"
        printf '%s\t%s\tPASS\n' "$version" "$bver" >> "$results"
      fi
    else
      echo "BOT_FAIL $version"
      tail -n 200 "$log" || true
      printf '%s\t%s\tBOT_FAIL\n' "$version" "$bver" >> "$results"
      failed=1
    fi
  fi
  stop_server
  sleep 2
done

cat "$results"
exit "$failed"
