#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RUN_DIR="$REPO_ROOT/run"
PAPER_VERSION="${PAPER_VERSION:-26.1.2}"
VELOCITY_VERSION="${VELOCITY_VERSION:-3.5.0-SNAPSHOT}"
VIA_VERSION="${VIA_VERSION:-5.9.1}"
JAVA_HOME_BUILD="${JAVA_HOME_BUILD:-/usr/lib/jvm/java-21-openjdk-amd64}"

mkdir -p "$RUN_DIR/paper/plugins" "$RUN_DIR/velocity/plugins" "$RUN_DIR/bot" "$RUN_DIR/logs" "$RUN_DIR/cache"

fill_url() {
  local project="$1" version="$2"
  curl -fsSL "https://fill.papermc.io/v3/projects/$project/versions/$version/builds" |
    node -e "const fs=require('fs'); const builds=JSON.parse(fs.readFileSync(0,'utf8')); const stable=builds.find(b=>b.channel==='STABLE')||builds[0]; if(!stable) process.exit(2); console.log(stable.downloads['server:default'].url);"
}

download_if_missing() {
  local url="$1" dest="$2"
  if [[ ! -s "$dest" ]]; then
    echo "Downloading $url"
    curl -fL "$url" -o "$dest"
  fi
}

paper_url="$(fill_url paper "$PAPER_VERSION")"
velocity_url="$(fill_url velocity "$VELOCITY_VERSION")"
download_if_missing "$paper_url" "$RUN_DIR/paper/server.jar"
download_if_missing "$velocity_url" "$RUN_DIR/velocity/server.jar"
download_if_missing "https://github.com/ViaVersion/ViaVersion/releases/download/$VIA_VERSION/ViaVersion-$VIA_VERSION.jar" "$RUN_DIR/paper/plugins/ViaVersion-$VIA_VERSION.jar"
download_if_missing "https://github.com/ViaVersion/ViaBackwards/releases/download/$VIA_VERSION/ViaBackwards-$VIA_VERSION.jar" "$RUN_DIR/paper/plugins/ViaBackwards-$VIA_VERSION.jar"

(cd "$REPO_ROOT" && JAVA_HOME="$JAVA_HOME_BUILD" ./gradlew shadowJar)
cp "$REPO_ROOT/Plugin/build/libs/NexAuth.jar" "$RUN_DIR/velocity/plugins/NexAuth.jar"

printf 'eula=true\n' > "$RUN_DIR/paper/eula.txt"
cat > "$RUN_DIR/paper/server.properties" <<'PROPS'
server-port=25566
online-mode=false
enforce-secure-profile=false
motd=NexAuth Local Backend
spawn-protection=0
view-distance=4
simulation-distance=4
PROPS

printf 'nexauth-local-dev-forwarding-secret\n' > "$RUN_DIR/velocity/forwarding.secret"
cat > "$RUN_DIR/velocity/velocity.toml" <<'TOML'
config-version = "2.8"
bind = "0.0.0.0:25565"
motd = "<#09add3>NexAuth Local Dev Proxy"
show-max-players = 500
online-mode = false
force-key-authentication = false
prevent-client-proxy-connections = false
player-info-forwarding-mode = "none"
forwarding-secret-file = "forwarding.secret"
announce-forge = false
kick-existing-players = false
ping-passthrough = "disabled"
sample-players-in-ping = false
enable-player-address-logging = true

[servers]
backend = "127.0.0.1:25566"
auth = "127.0.0.1:25566"
try = ["backend"]

[forced-hosts]

[advanced]
compression-threshold = 256
compression-level = -1
login-ratelimit = 3000
connection-timeout = 5000
read-timeout = 30000
haproxy-protocol = false
tcp-fast-open = false
bungee-plugin-message-channel = false
show-ping-requests = false
failover-on-unexpected-server-disconnect = true
announce-proxy-commands = true
log-command-executions = false
log-player-connections = true
accepts-transfers = false
TOML

cp "$REPO_ROOT/tools/test-env/bot/"*.js "$RUN_DIR/bot/"
cp "$REPO_ROOT/tools/test-env/bot/package.json" "$RUN_DIR/bot/package.json"
(cd "$RUN_DIR/bot" && npm install)

echo "Local test environment is ready under $RUN_DIR"
