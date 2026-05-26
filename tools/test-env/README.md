# NexAuth Local Test Environment

This directory preserves the local Velocity + Paper + mineflayer harness used for compatibility testing.

Generated files are written to the ignored `run/` directory:

- `run/paper/` - backend Paper server with ViaVersion and ViaBackwards.
- `run/velocity/` - Velocity proxy with `NexAuth.jar`.
- `run/bot/` - mineflayer debug and smoke bot dependencies.
- `run/paper-only/` - throwaway Paper-only matrix servers.
- `run/cache/` - downloaded Paper jars.

## Setup

```bash
tools/test-env/setup.sh
```

The setup script builds `NexAuth.jar`, downloads Paper, Velocity, ViaVersion, and ViaBackwards, and creates local configs.

## Proxy Stack

```bash
tools/test-env/bin/start-all.sh
tools/test-env/bin/bot.sh
tools/test-env/bin/stop-all.sh
```

The bot connects offline through Velocity on `127.0.0.1:25565`. Paper listens on `127.0.0.1:25566`.

## Paper-only Matrix

```bash
tools/test-env/bin/paper-boundary.sh
```

Override versions:

```bash
VERSIONS="1.21.10 1.21.11 26.1.2" tools/test-env/bin/paper-boundary.sh
```

The matrix only tests protocol/API boundary releases from `1.20` onward.
