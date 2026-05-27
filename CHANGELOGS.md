# NexAuth Release Notes

## NexAuth 0.0.1-beta4

### Beta Release

This beta continues NexAuth's modern Paper/Purpur and Velocity focus while carrying forward the latest compatibility and reliability work from the current codebase.

---

### What's New

#### Platform Support
- **0.0.1-beta4** release of NexAuth
- Support for Minecraft **1.13 through 1.21.11** and Paper **26.1.2**
- Updated Paper API target to **1.21.10-R0.1-SNAPSHOT**
- Ongoing compatibility with modern Velocity proxy environments

#### Authentication Platform Improvements
- **Integrated NexLimbo** as the recommended limbo system for NexAuth proxy setups
- Added operator-facing **doctor** and **status** commands for faster diagnostics
- Improved update-check reliability with layered GitHub API / feed / HTML fallbacks
- Unified `/nexauth reload` behavior with configuration diff display
- Removed BungeeCord and Waterfall platform support so maintenance can focus on Paper/Purpur and Velocity

### Reliability and Runtime Improvements

- **Velocity startup hardening** to reduce startup/login failures when dependent systems are still initializing
- **Runtime packaging cleanup** by moving Netty to Libby runtime download handling
- **Adventure compatibility fix** so version forcing only applies to runtime configurations
- **Null-safety improvements** around database lookup failures in platform listeners
- **Paper premium auth hardening** so Mojang session-server errors fail closed instead of being treated as valid joins
- **Locale-safe premium username normalization** with fail-closed handling when authoritative lookups are unavailable

### Supported Platforms

- **Paper/Purpur**: 1.13 - 1.21.11
- **Velocity**: Supported modern snapshot line
- **Java**: 21+

### Installation

1. Download `NexAuth.jar` from the assets below
2. Place it in your proxy/server `plugins` folder
3. Install **NexLimbo** for the complete proxy authentication limbo experience
4. Restart your server
5. Configure NexAuth for your deployment

### Important Notes

- **Beta Release**: Test before deploying to production and keep backups of your data.
- **Recommended Dependency**: Install NexLimbo on proxy setups for the best authentication flow.
- **Platform Scope**: BungeeCord and Waterfall are no longer shipped or maintained by NexAuth.

### Links

- **Repository**: https://github.com/Xreatlabs/NexAuth
- **Documentation**: https://github.com/Xreatlabs/NexAuth/wiki
- **Issues**: https://github.com/Xreatlabs/NexAuth/issues
- **Support**: https://discord.gg/xreatlabs

---

**Full Changelog**: https://github.com/Xreatlabs/NexAuth/compare/0.0.1-beta3...0.0.1-beta4
