# NexAuth Release Notes

## 🎉 NexAuth 1.0.0

### 💬 Production Release

This is the first stable production release of **NexAuth**. It promotes the work completed across the beta cycle into a `1.0.0` baseline intended for real-world deployments across Paper and Velocity networks.

---

### 🚀 What's New

#### ✨ Stable Platform Support
- **Production-ready 1.0.0 release** of NexAuth
- Support for Minecraft **1.13 through 1.21.10**
- Updated Paper API target to **1.21.10-R0.1-SNAPSHOT**
- Ongoing compatibility with modern Velocity proxy environments

#### 🔧 Authentication Platform Improvements
- **Integrated NexLimbo** as the recommended limbo system for NexAuth proxy setups
- Added operator-facing **doctor** and **status** commands for faster diagnostics
- Improved update-check reliability with layered GitHub API / feed / HTML fallbacks
- Unified `/nexauth reload` behavior with configuration diff display

### 🛠️ Reliability and Runtime Improvements

- **Velocity startup hardening** to reduce startup/login failures when dependent systems are still initializing
- **Runtime packaging cleanup** by moving Netty to Libby runtime download handling
- **Adventure compatibility fix** so version forcing only applies to runtime configurations
- **Null-safety improvements** around database lookup failures in platform listeners

### 📋 Supported Platforms

- **Paper/Purpur**: 1.13 - 1.21.10
- **Velocity**: Supported modern snapshot line
- **Java**: 21+

### 📦 Installation

1. Download `NexAuth.jar` from the assets below
2. Place it in your proxy/server `plugins` folder
3. Install **NexLimbo** for the complete proxy authentication limbo experience
4. Restart your server
5. Configure NexAuth for your deployment

### ⚠️ Important Notes

- **Production Ready**: This release is intended for production use.
- **Recommended Dependency**: Install NexLimbo on proxy setups for the best authentication flow.
- **Backups**: Always back up your data before updating.

### 🔗 Links

- **Repository**: https://github.com/Xreatlabs/NexAuth
- **Documentation**: https://github.com/Xreatlabs/NexAuth/wiki
- **Issues**: https://github.com/Xreatlabs/NexAuth/issues
- **Support**: https://discord.gg/xreatlabs

---

**Full Changelog**: https://github.com/Xreatlabs/NexAuth/compare/0.0.1-beta3...v1.0.0
