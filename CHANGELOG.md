# Changelog

All notable changes to this project will be documented in this file.

The format is inspired by [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Removed the BungeeCord/Waterfall platform implementation, plugin descriptor, upload loaders, and Bungee-only dependencies.
- Preserved the Velocity + Paper + mineflayer local smoke environment under `tools/test-env`.
- Updated Paper compatibility for Minecraft `1.21.10`, `1.21.11`, and `26.1.2`.

### Fixed
- Relocated `net.kyori.option` with Configurate to avoid Paper classloader conflicts.
- Updated PacketEvents to fix login/configuration packet parsing on newer Paper versions.
- Added Paper encryption reflection support for the newer `net.minecraft.util.Crypt#getCipher` runtime path.
- Hardened Paper premium session verification so Mojang session-server errors fail closed instead of being treated as valid joins.
- Made premium username normalization locale-safe and fail closed when Mojang's authoritative lookup is unavailable and fallback APIs miss.
- Required an already authorized, registered session before enabling, confirming, or disabling premium login from player commands.

## [1.0.0] - 2026-03-18

### Added
- Integrated **NexLimbo** as the recommended limbo system for proxy-based authentication flows.
- Added support for Minecraft **1.21.5** through **1.21.10**.
- Added operator-facing doctor and status diagnostics to improve production observability.
- Added packet-level inventory hiding for unauthenticated players.
- Added premium-player title support on auto-login.

### Changed
- Promoted NexAuth to its first stable production release.
- Updated the Paper API target to **1.21.10-R0.1-SNAPSHOT**.
- Updated Velocity compatibility to the latest supported snapshot line.
- Replaced NanoLimboPlugin dependency references with NexLimbo-focused integration and documentation.
- Improved update-check reliability with GitHub API, RSS feed, and HTML parsing fallbacks.
- Updated branding and default database naming to use **nexauth** consistently.
- Unified `/nexauth reload` behavior with configuration diff display.

### Fixed
- Hardened Velocity startup authentication flow to avoid startup/login failures when dependent systems are not fully ready.
- Added null-safety guards for database lookup failures in platform listeners.
- Fixed Adventure dependency forcing so compatibility constraints apply only to runtime configurations.
- Improved release runtime packaging by switching Netty to Libby runtime download instead of shading it directly.

### Security
- Continued support for secure password hashing, TOTP 2FA, and premium-account authentication flows across supported platforms.

## [0.0.1-beta3] - 2025-10-09

### Added
- Added support for Minecraft **1.21.5** through **1.21.10**.
- Integrated **NexLimbo** as the official limbo system for NexAuth.
- Replaced NanoLimboPlugin dependency references with NexLimbo integration across the project.

### Changed
- Updated the Paper API target to **1.21.10-R0.1-SNAPSHOT**.
- Updated build configuration for better dependency management.
- Integrated NexLimbo as a local subproject for tighter integration.
- Renamed limbo-related configuration references to NexLimbo naming.

### Fixed
- Fixed Paper **1.21.10** compatibility by restricting Adventure version forcing to runtime configurations only.

## [0.0.1-beta2] - 2025-07-16

### Added
- Added premium account titles for premium-player auto-login.
- Added packet-level inventory hiding for unauthenticated players.
- Added enhanced update-check fallback methods.

### Changed
- Updated all database references from `librelogin` to `nexauth`.
- Rebranded command permissions and project branding throughout the codebase.
- Improved title handling to use the existing `use-titles` configuration option.

### Fixed
- Improved update-checker error handling.

## [0.0.1-beta] - 2025-07-15

### Added
- Initial beta release of **NexAuth**.
- Added support for Minecraft **1.21.7**.
- Added legacy multi-platform support.
- Added support for MySQL, PostgreSQL, and SQLite.
- Added TOTP 2FA, premium integration, email support, and migration tooling.

### Changed
- Updated NanoLimboPlugin API usage to **1.0.15**.
- Updated GitHub links and documentation references to the NexAuth repository.
- Optimized shadow JAR generation and release API usage.
- Fixed semantic-version parsing for beta releases.

### Security
- Documented the beta release as pre-production and recommended backups/testing before use.

[Unreleased]: https://github.com/Xreatlabs/NexAuth/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/Xreatlabs/NexAuth/compare/0.0.1-beta3...v1.0.0
[0.0.1-beta3]: https://github.com/Xreatlabs/NexAuth/compare/0.0.1-beta2...0.0.1-beta3
[0.0.1-beta2]: https://github.com/Xreatlabs/NexAuth/compare/0.0.1-beta...0.0.1-beta2
[0.0.1-beta]: https://github.com/Xreatlabs/NexAuth/releases/tag/0.0.1-beta
