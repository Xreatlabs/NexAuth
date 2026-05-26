# NexAuth

NexAuth is a Java 21 authentication plugin for modern Minecraft networks. It supports Velocity proxy deployments and direct Paper/Purpur server deployments, with premium auto-login, offline registration/login, TOTP support, database-backed users, and a built-in Paper limbo/runtime path.

NexAuth began as a LibreLogin fork and now focuses on current Paper and Velocity behavior. BungeeCord and Waterfall support has been removed.

## Status

![Java 21+](https://img.shields.io/badge/Java-21%2B-blue?style=for-the-badge)
![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Velocity-0b7285?style=for-the-badge)
![License](https://img.shields.io/badge/License-MPL--2.0-2f9e44?style=for-the-badge)

## Features

- Offline registration and login for cracked/offline-mode players.
- Premium account auto-login and migration flows.
- TOTP two-factor authentication support.
- Session handling, name validation, and case-sensitivity checks.
- SQLite, MySQL/MariaDB, and PostgreSQL storage support.
- Geyser/Floodgate support for Bedrock players.
- Paper inventory hiding for unauthenticated players.
- Velocity proxy flow with NexLimbo-compatible limbo integration.
- Operator diagnostics through status/doctor tooling.

## Supported Platforms

| Platform | Status | Notes |
| --- | --- | --- |
| Velocity | Supported | Install `NexAuth.jar` on the proxy. Use a secured backend Paper server. |
| Paper/Purpur | Supported | Install `NexAuth.jar` directly on the server. The server must run in offline mode. |
| BungeeCord/Waterfall | Removed | The Bungee platform implementation and descriptor are no longer built or shipped. |

## Requirements

- Java 21 or newer for normal builds and current Paper/Velocity targets.
- Java 25 for testing Paper `26.1.x` locally.
- Paper/Purpur or Velocity.
- `online-mode=false` where NexAuth is installed.
- Backend servers must not be exposed directly to the public internet in proxy deployments.

## Installation

### Velocity Proxy

1. Build or download `NexAuth.jar`.
2. Place it in the Velocity `plugins/` directory.
3. Configure backend servers in Velocity.
4. Keep NexAuth on the proxy only for proxy-mode testing and production proxy deployments.
5. Restart Velocity and complete the generated NexAuth configuration.

### Paper/Purpur Server

1. Build or download `NexAuth.jar`.
2. Place it in the Paper/Purpur `plugins/` directory.
3. Set `online-mode=false` in `server.properties`.
4. Restart once to generate configuration, fill it out, then restart again.

## Local Development

Build and test:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :Plugin:test shadowJar
```

Set up the preserved local test environment:

```bash
tools/test-env/setup.sh
```

Run the Velocity + Paper backend stack:

```bash
tools/test-env/bin/start-all.sh
tools/test-env/bin/bot.sh
tools/test-env/bin/stop-all.sh
```

Run the optimized Paper-only compatibility matrix:

```bash
tools/test-env/bin/paper-boundary.sh
```

The matrix checks protocol/API boundary versions from Minecraft `1.20` through the latest Paper line instead of repeating every patch release.

## Repository Layout

- `API/` - public API contracts and shared API tests.
- `Plugin/` - platform implementations, shared authentication logic, embedded limbo server, resources, and tests.
- `Plugin/src/main/java/xyz/xreatlabs/nexauth/velocity/` - Velocity platform adapter.
- `Plugin/src/main/java/xyz/xreatlabs/nexauth/paper/` - Paper/Purpur platform adapter.
- `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/` - platform-neutral authentication, config, database, commands, metrics, and diagnostics.
- `Plugin/src/main/java/ua/nanit/limbo/` - embedded limbo protocol/server implementation.
- `tools/test-env/` - reproducible local Velocity/Paper/mineflayer smoke environment.
- `run/` - ignored generated runtime area used by local smoke tests.

## Credits

- [kyngs](https://github.com/kyngs), original LibreLogin author.
- [FastLogin contributors](https://github.com/games647/FastLogin), whose work helped inform the Paper premium login path.
- [Fejby](https://github.com/Fejby), for Floodgate testing support.

## License

NexAuth is free and open-source software licensed under the Mozilla Public License 2.0. See [LICENSE](LICENSE).
