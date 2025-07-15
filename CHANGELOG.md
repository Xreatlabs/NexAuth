# NexAuth Changelog

## [0.0.1-beta] - 2025-07-15

### 🎉 **Major Refactoring Release**

This is the initial beta release of **NexAuth** (formerly LibreLogin), featuring a complete rebrand and ownership transfer.

### 🔄 **Latest Updates**

- **Minecraft 1.21.7**: Added support for Minecraft 1.21.7
- **NanoLimboPlugin API**: Updated to version 1.0.15 for latest features and improvements
- **Velocity Support**: Enhanced compatibility with latest Velocity snapshots
- **Performance**: Improved limbo server integration and stability

### 🔄 **Breaking Changes**

- **Plugin Name**: LibreLogin → **NexAuth**
- **Author**: kyngs → **xreatlabs**
- **Package Structure**: `xyz.kyngs.librelogin` → `xyz.xreatlabs.nexauth`
- **Command**: `/librelogin` → `/nexauth`
- **JAR Name**: `LibreLogin.jar` → `NexAuth.jar`

### 🏗️ **Technical Changes**

#### Package & Class Updates
- Updated all **189 Java files** with new package declarations
- Renamed core classes:
  - `LibreLoginProvider` → `NexAuthProvider`
  - `LibreLoginPlugin` → `NexAuthPlugin`
  - `AuthenticLibreLogin` → `AuthenticNexAuth`
  - `BungeeCordLibreLogin` → `BungeeCordNexAuth`
  - `VelocityLibreLogin` → `VelocityNexAuth`
  - `PaperLibreLogin` → `PaperNexAuth`
  - `LibreLoginCommand` → `NexAuthCommand`
  - Database providers: `LibreLogin*DatabaseProvider` → `NexAuth*DatabaseProvider`

#### Configuration Updates
- Updated `plugin.yml`, `bungee.yml`, and `paper-plugin.yml`
- Changed Velocity plugin ID from `librelogin` to `nexauth`
- Updated library relocation paths to use `xyz.xreatlabs.nexauth.lib.*`

#### Build System
- Updated Gradle group ID: `xyz.kyngs.librelogin` → `xyz.xreatlabs.nexauth`
- Updated project name in `settings.gradle`
- Updated repository URLs and artifact names
- Shadow JAR now generates `NexAuth.jar`

### 📚 **Documentation**

- Updated `README.md` with new branding and URLs
- Changed GitHub repository references: `kyngs/LibreLogin` → `xreatlabs/NexAuth`
- Updated all documentation links and references

### 🔧 **Internal Changes**

- Updated error messages and log outputs to reference NexAuth
- Changed development build warnings to mention NexAuth
- Updated database connection pool names
- Fixed world generator registration: `librelogin:void` → `nexauth:void`
- Updated plugin directory paths for library management

### 🛠️ **API Changes**

- **Method Rename**: `getLibreLogin()` → `getNexAuth()` in `NexAuthProvider`
- All API interfaces now use `NexAuth` naming convention
- Updated JavaDoc references and examples

### 🎯 **What's Preserved**

- **Full Functionality**: All original features remain intact
- **Database Compatibility**: Existing databases continue to work
- **Configuration Compatibility**: Current config files remain valid
- **User Data**: All user accounts and settings preserved
- **Plugin Integrations**: LuckPerms, Floodgate, etc. continue to work

### 🔍 **Version Information**

- **Version**: 0.0.1-beta (pre-production)
- **Java**: Requires Java 17+
- **Platforms**: Paper, Velocity, BungeeCord
- **License**: Mozilla Public License 2.0

### ⚠️ **Beta Notice**

This is a **pre-production beta release** intended for testing purposes. While all functionality has been preserved, please:

1. **Backup your data** before upgrading
2. **Test thoroughly** in a development environment
3. **Report any issues** to the new repository
4. **Update your documentation** to reference NexAuth

### 🚀 **Migration from LibreLogin**

To migrate from LibreLogin to NexAuth:

1. **Stop your server**
2. **Remove** the old `LibreLogin.jar`
3. **Install** `NexAuth.jar` in your plugins folder
4. **Update commands** from `/librelogin` to `/nexauth`
5. **Start your server**

No configuration changes are required - all settings will be automatically migrated.

### 🔗 **Links**

- **Repository**: https://github.com/xreatlabs/NexAuth
- **Documentation**: https://github.com/xreatlabs/NexAuth/wiki
- **Issues**: https://github.com/xreatlabs/NexAuth/issues
- **Contributors**: https://github.com/xreatlabs/NexAuth/graphs/contributors

---

## Previous LibreLogin Releases

### 0.24.0 - 1.21.1 - 1.21.4 Support

- Add support for 1.21.4
- Add support for Java 23
- Fix "logged in from another location" issue on Paper (see GH #296)

---

**Note**: This changelog represents the complete refactoring from LibreLogin to NexAuth. Future releases will follow standard semantic versioning.