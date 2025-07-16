# NexAuth Changelog

## [0.0.1-beta2] - 2025-07-16

### 🎉 **New Features**

- **Premium Account Titles**: Added title display for premium players on auto-login
- **Inventory Hiding**: Implemented packet-level inventory hiding for unauthenticated players
- **Enhanced Update Checker**: Added multiple fallback methods for better reliability

### 🔄 **Updates**

- **Configuration**: Updated all database references from "librelogin" to "nexauth"
- **Branding**: Complete rebranding throughout the codebase
- **Update System**: Improved update checking with GitHub API, RSS feed, and HTML parsing fallbacks
- **Title System**: Premium titles now use existing `use-titles` configuration option

### 🛠️ **Technical Improvements**

- **PacketEvents Integration**: Added packet-level inventory hiding using PacketEvents 2.7.0
- **Database Connectors**: Updated default database names to use "nexauth" branding
- **Command Permissions**: Updated all permission nodes to use "nexauth" prefix
- **Error Handling**: Enhanced error handling in update checker system

## [0.0.1-beta] - 2025-07-15

### 🎉 **Initial NexAuth Release**

This is the initial beta release of **NexAuth**, a modern authentication plugin forked and rebranded for enhanced security and performance.

### 🔄 **Latest Updates**

- **Minecraft 1.21.7**: Added support for Minecraft 1.21.7
- **NanoLimboPlugin API**: Updated to version 1.0.15 for latest features and improvements
- **Velocity Support**: Enhanced compatibility with latest Velocity snapshots
- **Performance**: Improved limbo server integration and stability
- **GitHub Integration**: Updated all links to use new repository at https://github.com/Xreatlabs/NexAuth
- **Version Parsing**: Fixed semantic version parsing for beta releases
- **Forbidden Passwords**: Updated to use new GitHub repository for password list

### 🛠️ **Technical Improvements**

- **API Compatibility**: Enhanced NanoLimboPlugin integration with secure profile support
- **Configuration**: Updated all GitHub links and documentation references
- **Build System**: Optimized shadow JAR generation for better performance
- **Update Checker**: Configured to use new GitHub releases API

### 🔧 **Features**

- **Multi-platform Support**: Paper, Velocity, BungeeCord
- **Database Support**: MySQL, PostgreSQL, SQLite
- **Authentication**: Secure password hashing with multiple algorithms
- **TOTP 2FA**: Time-based One-Time Password support
- **Premium Integration**: Automatic premium player detection
- **Limbo Server**: Integrated authentication limbo experience
- **Email Support**: Password reset via email
- **Migration Tools**: Import from various auth plugins

### 📋 **Version Information**

- **Version**: 0.0.1-beta (pre-production)
- **Java**: Requires Java 17+
- **Platforms**: Paper, Velocity, BungeeCord
- **License**: Mozilla Public License 2.0

### ⚠️ **Beta Notice**

This is a **pre-production beta release** intended for testing purposes. Please:

1. **Backup your data** before installation
2. **Test thoroughly** in a development environment
3. **Report any issues** to the repository
4. **Update your documentation** to reference NexAuth

### 🔗 **Links**

- **Repository**: https://github.com/Xreatlabs/NexAuth
- **Documentation**: https://github.com/Xreatlabs/NexAuth/wiki
- **Issues**: https://github.com/Xreatlabs/NexAuth/issues
- **Contributors**: https://github.com/Xreatlabs/NexAuth/graphs/contributors

---

**Note**: This is a complete rebranding and enhancement of the original authentication plugin with improved security, performance, and modern features.