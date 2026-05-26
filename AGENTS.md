# NexAuth Agent Index

This file is the project map for coding agents working in this repository. Follow it before making broad changes.

## Project Summary

NexAuth is a Java 21 Minecraft authentication plugin. It builds one shaded `NexAuth.jar` from two Gradle modules:

- `API/` exposes public API contracts used by integrations and tests.
- `Plugin/` contains the runtime implementation for Velocity and Paper/Purpur.

BungeeCord and Waterfall support has been removed. Do not reintroduce Bungee platform classes, `bungee.yml`, Bungee loaders, or Bungee-only dependencies unless the user explicitly asks for a new platform effort.

## Supported Runtime Architecture

### Velocity Mode

Velocity mode installs `NexAuth.jar` on the Velocity proxy. Authentication happens before players reach backend servers. The Velocity adapter lives in:

- `Plugin/src/main/java/xyz/xreatlabs/nexauth/velocity/`

Velocity may use:

- `VelocityNexAuth` for lifecycle and platform integration.
- `VelocityListeners` and `Blockers` for login/auth flow.
- `VelocityNativeLimboIntegration` for local limbo integration.
- `VelocityRedisBungeeIntegration` for optional RedisBungee-compatible multi-proxy presence checks. The name is historical and belongs to the RedisBungee API, not Bungee platform support.

### Paper Mode

Paper mode installs `NexAuth.jar` directly on Paper/Purpur. The server must run offline mode. The Paper adapter lives in:

- `Plugin/src/main/java/xyz/xreatlabs/nexauth/paper/`

Important classes:

- `PaperBootstrap` is the Bukkit/Paper plugin entrypoint.
- `PaperNexAuth` adapts common auth logic to Paper.
- `PaperListeners` handles login/pre-login flow, premium checks, encryption handoff, and spawn routing.
- `paper/protocol/*` contains PacketEvents-based packet handling and inventory hiding.

### Shared Core

Shared auth logic lives in:

- `Plugin/src/main/java/xyz/xreatlabs/nexauth/common/`

This package owns configuration, database providers, commands, metrics, doctor/status diagnostics, mail, premium auth, rate limiting, and shared listener behavior.

### Embedded Limbo

The embedded limbo/protocol server lives in:

- `Plugin/src/main/java/ua/nanit/limbo/`

This code handles protocol-level compatibility. Packet mappings for new Minecraft versions belong under `ua/nanit/limbo/protocol/registry` and packet implementations under `ua/nanit/limbo/protocol/packets`.

## Build and Test Commands

Use Java 21 for normal builds:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew :Plugin:test shadowJar
```

Use Java 25 only for running latest Paper `26.1.x` servers in the local smoke environment.

Run the optimized direct-Paper matrix:

```bash
tools/test-env/bin/paper-boundary.sh
```

This checks protocol/API boundary versions from `1.20` through current Paper rather than every patch release.

## Local Test Environment

Tracked harness files live in:

- `tools/test-env/`

Generated runtime state lives in ignored `run/`.

Setup:

```bash
tools/test-env/setup.sh
```

Velocity + backend Paper stack:

```bash
tools/test-env/bin/start-all.sh
tools/test-env/bin/bot.sh
tools/test-env/bin/stop-all.sh
```

Mineflayer smoke scripts live under `tools/test-env/bot/`.

## Dependency Rules

- Runtime third-party libraries are declared with Libby in `Plugin/build.gradle.kts`.
- Keep platform-specific dependencies scoped to their platform.
- Keep `net.kyori.option` relocated with Configurate. Without it, Paper can split Kyori option classes across classloaders.
- PacketEvents is required by Paper packet listeners and inventory hiding.
- Velocity RedisBungee support should use the Velocity artifact, not Bungee platform APIs.

## Editing Rules

- Keep platform adapters separate. Do not let Paper import Velocity classes or Velocity import Paper classes.
- Shared behavior belongs in `common/` only when it is truly platform-neutral.
- Do not put generated server files, downloaded jars, logs, worlds, or `node_modules` into git.
- Do not delete ignored `run/` runtime files unless the task is specifically about cleaning the local smoke environment.
- If changing protocol compatibility, add or update focused tests under `Plugin/src/test/java/ua/nanit/limbo/`.
- If changing auth flow, run unit tests and at least one relevant smoke test.

## Current Compatibility Notes

- Paper-only matrix is green for boundary versions `1.20`, `1.20.2`, `1.20.4`, `1.20.6`, `1.21`, `1.21.3`, `1.21.4`, `1.21.5`, `1.21.6`, `1.21.8`, `1.21.10`, `1.21.11`, and `26.1.2`.
- Mineflayer currently uses `1.21.11` when connecting through ViaBackwards to Paper `26.1.2`.
- NexAuth intentionally shuts down after first-time config generation; smoke harnesses account for this with a two-phase boot.
