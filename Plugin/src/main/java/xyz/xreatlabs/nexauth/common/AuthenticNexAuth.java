/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.xreatlabs.nexauth.common;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.kyori.adventure.audience.Audience;
import org.bstats.charts.CustomChart;
import org.jetbrains.annotations.Nullable;
import xyz.xreatlabs.nexauth.api.BiHolder;
import xyz.xreatlabs.nexauth.api.NexAuthPlugin;
import xyz.xreatlabs.nexauth.api.Logger;
import xyz.xreatlabs.nexauth.api.PlatformHandle;
import xyz.xreatlabs.nexauth.api.configuration.CorruptedConfigurationException;
import xyz.xreatlabs.nexauth.api.crypto.CryptoProvider;
import xyz.xreatlabs.nexauth.api.crypto.HashedPassword;
import xyz.xreatlabs.nexauth.api.database.*;
import xyz.xreatlabs.nexauth.api.database.connector.DatabaseConnector;
import xyz.xreatlabs.nexauth.api.database.connector.MySQLDatabaseConnector;
import xyz.xreatlabs.nexauth.api.database.connector.PostgreSQLDatabaseConnector;
import xyz.xreatlabs.nexauth.api.database.connector.SQLiteDatabaseConnector;
import xyz.xreatlabs.nexauth.api.integration.LimboIntegration;
import xyz.xreatlabs.nexauth.api.premium.PremiumException;
import xyz.xreatlabs.nexauth.api.premium.PremiumUser;
import xyz.xreatlabs.nexauth.api.server.ServerHandler;
import xyz.xreatlabs.nexauth.api.totp.TOTPProvider;
import xyz.xreatlabs.nexauth.api.util.Release;
import xyz.xreatlabs.nexauth.api.util.SemanticVersion;
import xyz.xreatlabs.nexauth.api.util.ThrowableFunction;
import xyz.xreatlabs.nexauth.common.authorization.AuthenticAuthorizationProvider;
import xyz.xreatlabs.nexauth.common.command.CommandProvider;
import xyz.xreatlabs.nexauth.common.command.InvalidCommandArgument;
import xyz.xreatlabs.nexauth.common.config.HoconMessages;
import xyz.xreatlabs.nexauth.common.config.HoconPluginConfiguration;
import xyz.xreatlabs.nexauth.common.crypto.Argon2IDCryptoProvider;
import xyz.xreatlabs.nexauth.common.crypto.BCrypt2ACryptoProvider;
import xyz.xreatlabs.nexauth.common.crypto.LogITMessageDigestCryptoProvider;
import xyz.xreatlabs.nexauth.common.crypto.MessageDigestCryptoProvider;
import xyz.xreatlabs.nexauth.common.database.AuthenticDatabaseProvider;
import xyz.xreatlabs.nexauth.common.database.AuthenticUser;
import xyz.xreatlabs.nexauth.common.database.connector.AuthenticMySQLDatabaseConnector;
import xyz.xreatlabs.nexauth.common.database.connector.AuthenticPostgreSQLDatabaseConnector;
import xyz.xreatlabs.nexauth.common.database.connector.AuthenticSQLiteDatabaseConnector;
import xyz.xreatlabs.nexauth.common.database.connector.DatabaseConnectorRegistration;
import xyz.xreatlabs.nexauth.common.database.provider.NexAuthMySQLDatabaseProvider;
import xyz.xreatlabs.nexauth.common.database.provider.NexAuthPostgreSQLDatabaseProvider;
import xyz.xreatlabs.nexauth.common.database.provider.NexAuthSQLiteDatabaseProvider;
import xyz.xreatlabs.nexauth.common.event.AuthenticEventProvider;
import xyz.xreatlabs.nexauth.common.image.AuthenticImageProjector;
import xyz.xreatlabs.nexauth.common.integration.FloodgateIntegration;
import xyz.xreatlabs.nexauth.common.integration.luckperms.LuckPermsIntegration;
import xyz.xreatlabs.nexauth.common.listener.LoginTryListener;
import xyz.xreatlabs.nexauth.common.log.Log4JFilter;
import xyz.xreatlabs.nexauth.common.log.SimpleLogFilter;
import xyz.xreatlabs.nexauth.common.mail.AuthenticEMailHandler;
import xyz.xreatlabs.nexauth.common.migrate.*;
import xyz.xreatlabs.nexauth.common.premium.AuthenticPremiumProvider;
import xyz.xreatlabs.nexauth.common.server.AuthenticServerHandler;
import xyz.xreatlabs.nexauth.common.totp.AuthenticTOTPProvider;
import xyz.xreatlabs.nexauth.common.util.CancellableTask;
import xyz.xreatlabs.nexauth.common.util.GeneralUtil;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import static xyz.xreatlabs.nexauth.common.config.ConfigurationKeys.*;

public abstract class AuthenticNexAuth<P, S> implements NexAuthPlugin<P, S> {

    public static final Gson GSON = new Gson();
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd. MM. yyyy HH:mm");
    public static final ExecutorService EXECUTOR;

    static {
        EXECUTOR = new ForkJoinPool(4);
    }

    private final Map<String, CryptoProvider> cryptoProviders;
    private final Map<String, ReadDatabaseProviderRegistration<?, ?, ?>> readProviders;
    private final Map<Class<?>, DatabaseConnectorRegistration<?, ?>> databaseConnectors;
    private final Multimap<P, CancellableTask> cancelOnExit;
    private final PlatformHandle<P, S> platformHandle;
    private final Set<String> forbiddenPasswords;
    protected Logger logger;
    private AuthenticPremiumProvider premiumProvider;
    private AuthenticEventProvider<P, S> eventProvider;
    private AuthenticServerHandler<P, S> serverHandler;
    private TOTPProvider totpProvider;
    private AuthenticImageProjector<P, S> imageProjector;
    private FloodgateIntegration floodgateApi;
    private LuckPermsIntegration<P, S> luckpermsApi;
    private SemanticVersion version;
    private HoconPluginConfiguration configuration;
    private HoconMessages messages;
    private AuthenticAuthorizationProvider<P, S> authorizationProvider;
    private CommandProvider<P, S> commandProvider;
    private ReadWriteDatabaseProvider databaseProvider;
    private DatabaseConnector<?, ?> databaseConnector;
    private AuthenticEMailHandler eMailHandler;
    private LoginTryListener<P, S> loginTryListener;

    protected AuthenticNexAuth() {
        cryptoProviders = new ConcurrentHashMap<>();
        readProviders = new ConcurrentHashMap<>();
        databaseConnectors = new ConcurrentHashMap<>();
        platformHandle = providePlatformHandle();
        forbiddenPasswords = new HashSet<>();
        cancelOnExit = HashMultimap.create();
    }

    public Map<Class<?>, DatabaseConnectorRegistration<?, ?>> getDatabaseConnectors() {
        return databaseConnectors;
    }

    @Override
    public <E extends Exception, C extends DatabaseConnector<E, ?>> void registerDatabaseConnector(Class<?> clazz, ThrowableFunction<String, C, E> factory, String id) {
        registerDatabaseConnector(new DatabaseConnectorRegistration<>(factory, null, id), clazz);
    }

    @Override
    public void registerReadProvider(ReadDatabaseProviderRegistration<?, ?, ?> registration) {
        readProviders.put(registration.id(), registration);
    }

    @Override
    public AuthenticEMailHandler getEmailHandler() {
        return eMailHandler;
    }

    @Nullable
    @Override
    public LimboIntegration<S> getLimboIntegration() {
        return null;
    }

    @Override
    public User createUser(UUID uuid, UUID premiumUUID, HashedPassword hashedPassword, String lastNickname, Timestamp joinDate, Timestamp lastSeen, String secret, String ip, Timestamp lastAuthentication, String lastServer, String email) {
        return new AuthenticUser(uuid, premiumUUID, hashedPassword, lastNickname, joinDate, lastSeen, secret, ip, lastAuthentication, lastServer, email);
    }

    public void registerDatabaseConnector(DatabaseConnectorRegistration<?, ?> registration, Class<?> clazz) {
        databaseConnectors.put(clazz, registration);
    }

    @Override
    public PlatformHandle<P, S> getPlatformHandle() {
        return platformHandle;
    }

    protected abstract PlatformHandle<P, S> providePlatformHandle();

    @Override
    public SemanticVersion getParsedVersion() {
        return version;
    }


    @Override
    public boolean validPassword(String password) {
        var length = password.length() >= configuration.get(MINIMUM_PASSWORD_LENGTH);

        if (!length) {
            return false;
        }

        var upper = password.toUpperCase();

        return !forbiddenPasswords.contains(upper);
    }

    @Override
    public Map<String, ReadDatabaseProviderRegistration<?, ?, ?>> getReadProviders() {
        return Map.copyOf(readProviders);
    }

    public CommandProvider<P, S> getCommandProvider() {
        return commandProvider;
    }

    @Override
    public ReadWriteDatabaseProvider getDatabaseProvider() {
        return databaseProvider;
    }

    @Override
    public AuthenticPremiumProvider getPremiumProvider() {
        return premiumProvider;
    }

    @Override
    public TOTPProvider getTOTPProvider() {
        return totpProvider;
    }

    @Override
    public AuthenticImageProjector<P, S> getImageProjector() {
        return imageProjector;
    }

    @Override
    public ServerHandler<P, S> getServerHandler() {
        return serverHandler;
    }

    protected void enable() {
        version = SemanticVersion.parse(getVersion());
        if (logger == null) logger = provideLogger();

        try {
            new Log4JFilter().inject();
        } catch (Throwable ignored) {
            logger.info("LogFilter is not supported on this platform");
            var simpleLogger = getSimpleLogger();

            if (simpleLogger != null) {
                logger.info("Using SimpleLogFilter");
                new SimpleLogFilter(simpleLogger).inject();
            }
        }

        var folder = getDataFolder();
        if (!folder.exists()) {
            var oldFolder = new File(folder.getParentFile(), folder.getName().equals("nexauth") ? "librepremium" : "LibrePremium");
            if (oldFolder.exists()) {
                logger.info("Migrating configuration and messages from old folder...");
                if (!oldFolder.renameTo(folder)) {
                    throw new RuntimeException("Can't migrate configuration and messages from old folder!");
                }
            }
        }

        try {
            Files.copy(getResourceAsStream("LICENSE.txt"), new File(folder, "LICENSE.txt").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
            // Silently ignore
        }

        if (platformHandle.getPlatformIdentifier().equals("paper")) {
            LIMBO.setDefault(List.of("limbo"));

            var lobby = HashMultimap.<String, String>create();
            lobby.put("root", "world");

            LOBBY.setDefault(lobby);
        }

        eventProvider = new AuthenticEventProvider<>(this);
        premiumProvider = new AuthenticPremiumProvider(this);

        registerCryptoProvider(new MessageDigestCryptoProvider("SHA-256"));
        registerCryptoProvider(new MessageDigestCryptoProvider("SHA-512"));
        registerCryptoProvider(new BCrypt2ACryptoProvider());
        registerCryptoProvider(new Argon2IDCryptoProvider(logger));
        registerCryptoProvider(new LogITMessageDigestCryptoProvider("LOGIT-SHA-256", "SHA-256"));

        setupDB();

        checkDataFolder();

        loadConfigs();

        logger.info("Loading forbidden passwords...");

        try {
            loadForbiddenPasswords();
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("An unknown exception occurred while attempting to load the forbidden passwords, this most likely isn't your fault");
            shutdownProxy(1);
        }

        logger.info("Loaded %s forbidden passwords".formatted(forbiddenPasswords.size()));

        connectToDB();

        serverHandler = new AuthenticServerHandler<>(this);

        this.loginTryListener = new LoginTryListener<>(this);

        // Moved to a different class to avoid class loading issues
        GeneralUtil.checkAndMigrate(configuration, logger, this);

        imageProjector = provideImageProjector();

        if (imageProjector != null) {
            if (!configuration.get(TOTP_ENABLED)) {
                imageProjector = null;
                logger.warn("2FA is disabled in the configuration, aborting...");
            } else {
                imageProjector.enable();
            }
        }

        totpProvider = imageProjector == null ? null : new AuthenticTOTPProvider(this);
        eMailHandler = configuration.get(MAIL_ENABLED) ? new AuthenticEMailHandler(this) : null;

        authorizationProvider = new AuthenticAuthorizationProvider<>(this);
        commandProvider = new CommandProvider<>(this);

        if (version.dev()) {
            logger.warn("!! YOU ARE RUNNING A DEVELOPMENT BUILD OF NEXAUTH !!");
            logger.warn("!! THIS IS NOT A RELEASE, USE THIS ONLY IF YOU WERE INSTRUCTED TO DO SO. DO NOT USE THIS IN PRODUCTION !!");
        } else {
            initMetrics();
        }

        delay(this::checkForUpdates, 1000);

        if (pluginPresent("floodgate")) {
            logger.info("Floodgate detected, enabling bedrock support...");
            floodgateApi = new FloodgateIntegration();
        }

        if (pluginPresent("luckperms")) {
            logger.info("LuckPerms detected, enabling context provider");
            luckpermsApi = new LuckPermsIntegration<>(this);
        }

        if (multiProxyEnabled()) {
            logger.info("Detected MultiProxy setup, enabling MultiProxy support...");
        }
    }

    public <C extends DatabaseConnector<?, ?>> DatabaseConnectorRegistration<?, C> getDatabaseConnector(Class<C> clazz) {
        return (DatabaseConnectorRegistration<?, C>) databaseConnectors.get(clazz);
    }

    private void connectToDB() {
        logger.info("Connecting to the database...");

        try {
            var registration = readProviders.get(configuration.get(DATABASE_TYPE));
            if (registration == null) {
                logger.error("Database type %s doesn't exist, please check your configuration".formatted(configuration.get(DATABASE_TYPE)));
                shutdownProxy(1);
            }

            DatabaseConnector<?, ?> connector = null;

            if (registration.databaseConnector() != null) {
                var connectorRegistration = getDatabaseConnector(registration.databaseConnector());

                if (connectorRegistration == null) {
                    logger.error("Database type %s is corrupted, please use a different one".formatted(configuration.get(DATABASE_TYPE)));
                    shutdownProxy(1);
                }

                connector = connectorRegistration.factory().apply("database.properties." + connectorRegistration.id() + ".");

                connector.connect();
            }

            var provider = registration.create(connector);

            if (provider instanceof ReadWriteDatabaseProvider casted) {
                databaseProvider = casted;
                databaseConnector = connector;
            } else {
                logger.error("Database type %s cannot be used for writing, please use a different one".formatted(configuration.get(DATABASE_TYPE)));
                shutdownProxy(1);
            }

        } catch (Exception e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY NEXAUTH !!");
            logger.error("Failed to connect to the database, this most likely is caused by wrong credentials. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            shutdownProxy(1);
        }

        logger.info("Successfully connected to the database");

        if (databaseProvider instanceof AuthenticDatabaseProvider<?> casted) {
            logger.info("Validating schema");

            try {
                casted.validateSchema();
            } catch (Exception e) {
                var cause = GeneralUtil.getFurthestCause(e);
                logger.error("Failed to validate schema! Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
                logger.error("Please open an issue on our GitHub, or visit Discord support");
                shutdownProxy(1);
            }

            logger.info("Schema validated");
        }
    }

    private void loadConfigs() {
        logger.info("Loading messages...");

        messages = new HoconMessages(logger);

        try {
            messages.reload(this);
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("An unknown exception occurred while attempting to load the messages, this most likely isn't your fault");
            shutdownProxy(1);
        } catch (CorruptedConfigurationException e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY NEXAUTH !!");
            logger.error("!!The messages are corrupted, please look below for further clues. If you are clueless, delete the messages and a new ones will be created for you. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            shutdownProxy(1);
        }

        logger.info("Loading configuration...");

        var defaults = new ArrayList<BiHolder<Class<?>, String>>();

        for (DatabaseConnectorRegistration<?, ?> value : databaseConnectors.values()) {
            if (value.configClass() == null) continue;
            defaults.add(new BiHolder<>(value.configClass(), "database.properties." + value.id() + "."));
            defaults.add(new BiHolder<>(value.configClass(), "migration.old-database." + value.id() + "."));
        }

        configuration = new HoconPluginConfiguration(logger, defaults);

        try {
            if (configuration.reload(this)) {
                logger.warn("!! A new configuration was generated, please fill it out, if in doubt, see the wiki !!");
                shutdownProxy(0);
            }

            var limbos = configuration.get(LIMBO);
            var lobby = configuration.get(LOBBY);

            for (String value : lobby.values()) {
                if (limbos.contains(value)) {
                    throw new CorruptedConfigurationException("Lobby server/world %s is also a limbo server/world, this is not allowed".formatted(value));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("An unknown exception occurred while attempting to load the configuration, this most likely isn't your fault");
            shutdownProxy(1);
        } catch (CorruptedConfigurationException e) {
            var cause = GeneralUtil.getFurthestCause(e);
            logger.error("!! THIS IS MOST LIKELY NOT AN ERROR CAUSED BY NEXAUTH !!");
            logger.error("!!The configuration is corrupted, please look below for further clues. If you are clueless, delete the config and a new one will be created for you. Cause: %s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()));
            shutdownProxy(1);
        }
    }

    private void setupDB() {
        registerDatabaseConnector(new DatabaseConnectorRegistration<>(
                        prefix -> new AuthenticMySQLDatabaseConnector(this, prefix),
                        AuthenticMySQLDatabaseConnector.Configuration.class,
                        "mysql"
                ),
                MySQLDatabaseConnector.class);
        registerDatabaseConnector(new DatabaseConnectorRegistration<>(
                        prefix -> new AuthenticSQLiteDatabaseConnector(this, prefix),
                        AuthenticSQLiteDatabaseConnector.Configuration.class,
                        "sqlite"
                ),
                SQLiteDatabaseConnector.class);
        registerDatabaseConnector(new DatabaseConnectorRegistration<>(
                        prefix -> new AuthenticPostgreSQLDatabaseConnector(this, prefix),
                        AuthenticPostgreSQLDatabaseConnector.Configuration.class,
                        "postgresql"
                ),
                PostgreSQLDatabaseConnector.class);

        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new NexAuthMySQLDatabaseProvider(connector, this),
                "nexauth-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new NexAuthSQLiteDatabaseProvider(connector, this),
                "nexauth-sqlite",
                SQLiteDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new NexAuthPostgreSQLDatabaseProvider(connector, this),
                "nexauth-postgresql",
                PostgreSQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new AegisSQLMigrateReadProvider(configuration.get(MIGRATION_MYSQL_OLD_DATABASE_TABLE), logger, connector),
                "aegis-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new AuthMeSQLMigrateReadProvider(configuration.get(MIGRATION_MYSQL_OLD_DATABASE_TABLE), logger, connector),
                "authme-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new AuthMeSQLMigrateReadProvider(configuration.get(MIGRATION_POSTGRESQL_OLD_DATABASE_TABLE), logger, connector),
                "authme-postgresql",
                PostgreSQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new AuthMeSQLMigrateReadProvider("authme", logger, connector),
                "authme-sqlite",
                SQLiteDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new DBASQLMigrateReadProvider(configuration.get(MIGRATION_MYSQL_OLD_DATABASE_TABLE), logger, connector),
                "dba-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new JPremiumSQLMigrateReadProvider(configuration.get(MIGRATION_MYSQL_OLD_DATABASE_TABLE), logger, connector),
                "jpremium-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new NLoginSQLMigrateReadProvider("nlogin", logger, connector),
                "nlogin-sqlite",
                SQLiteDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new NLoginSQLMigrateReadProvider("nlogin", logger, connector),
                "nlogin-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new FastLoginSQLMigrateReadProvider("premium", logger, connector, databaseConnector, premiumProvider),
                "fastlogin-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new FastLoginSQLMigrateReadProvider("premium", logger, connector, databaseConnector, premiumProvider),
                "fastlogin-sqlite",
                SQLiteDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new UniqueCodeAuthSQLMigrateReadProvider("uniquecode_proxy_users", logger, connector, this),
                "uniquecodeauth-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new LoginSecuritySQLMigrateReadProvider("ls_players", logger, connector),
                "loginsecurity-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new LoginSecuritySQLMigrateReadProvider("ls_players", logger, connector),
                "loginsecurity-sqlite",
                SQLiteDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new LimboAuthSQLMigrateReadProvider("AUTH", logger, connector),
                "limboauth-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new AuthySQLMigrateReadProvider("players", logger, connector),
                "authy-mysql",
                MySQLDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new AuthySQLMigrateReadProvider("players", logger, connector),
                "authy-sqlite",
                SQLiteDatabaseConnector.class
        ));
        registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new LogItSQLMigrateReadProvider(configuration.get(MIGRATION_MYSQL_OLD_DATABASE_TABLE), logger, connector),
                "logit-mysql",
                MySQLDatabaseConnector.class
        ));
        // Currently disabled as crazylogin stores all names in lowercase
        /*registerReadProvider(new ReadDatabaseProviderRegistration<>(
                connector -> new CrazyLoginSQLMigrateReadProvider(configuration.get(MIGRATION_MYSQL_OLD_DATABASE_TABLE), logger, connector),
                "crazylogin-mysql",
                MySQLDatabaseConnector.class
        ));*/
    }

    private void loadForbiddenPasswords() throws IOException {
        var file = new File(getDataFolder(), "forbidden-passwords.txt");

        if (!file.exists()) {
            logger.info("Forbidden passwords list doesn't exist, downloading...");
            try (BufferedInputStream in = new BufferedInputStream(new URL("https://raw.githubusercontent.com/Xreatlabs/NexAuth/refs/heads/master/forbidden-passwords.txt").openStream())) {
                if (!file.createNewFile()) {
                    throw new IOException("Failed to create file");
                }
                try (var fos = new FileOutputStream(file)) {
                    var dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fos.write(dataBuffer, 0, bytesRead);
                    }
                }
                logger.info("Successfully downloaded forbidden passwords list");
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("Failed to download forbidden passwords list, using template instead");
                Files.copy(getResourceAsStream("forbidden-passwords-template.txt"), file.toPath());
            }
        }

        try (var reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("# ")) {
                    continue;
                }
                forbiddenPasswords.add(line.toUpperCase(Locale.ROOT));
            }
        }
    }

    private void checkForUpdates() {
        logger.info("Checking for updates...");

        try {
            // Use multiple endpoints for better reliability
            var updateInfo = checkLatestRelease();
            
            if (updateInfo == null) {
                logger.warn("Unable to check for updates - all methods failed");
                return;
            }

            List<Release> behind = new ArrayList<>();
            SemanticVersion latest = updateInfo.latest();
            
            // Check if we're behind
            var comparison = this.version.compare(latest);
            
            if (comparison < 0) {
                // We're behind, add all versions we're missing
                for (Release release : updateInfo.allReleases()) {
                    if (this.version.compare(release.version()) < 0) {
                        behind.add(release);
                    }
                }
            }

            if (behind.isEmpty()) {
                logger.info("You are running the latest version of NexAuth (%s)".formatted(getVersion()));
            } else {
                Collections.reverse(behind);
                logger.warn("!! YOU ARE RUNNING AN OUTDATED VERSION OF NEXAUTH !!");
                logger.info("Current version: %s | Latest version: %s | Versions behind: %d".formatted(
                    getVersion(), latest, behind.size()));
                
                // Show only the most recent updates (max 5)
                var recentUpdates = behind.subList(0, Math.min(behind.size(), 5));
                logger.info("Recent updates you're missing:");
                for (Release release : recentUpdates) {
                    logger.info("  → %s (%s)".formatted(release.name(), release.version()));
                }
                
                if (behind.size() > 5) {
                    logger.info("  ... and %d more versions".formatted(behind.size() - 5));
                }
                
                logger.warn("!! PLEASE UPDATE TO THE LATEST VERSION !!");
                logger.info("Download: https://github.com/Xreatlabs/NexAuth/releases/latest");
            }
        } catch (Exception e) {
            logger.warn("Failed to check for updates: %s".formatted(e.getMessage()));
            logger.debug("Update check error details:", e);
        }
    }

    private UpdateInfo checkLatestRelease() {
        // Method 1: Try GitHub API first
        try {
            return checkGitHubAPI();
        } catch (Exception e) {
            logger.debug("GitHub API check failed: %s".formatted(e.getMessage()));
        }
        
        // Method 2: Try GitHub releases RSS feed as backup
        try {
            return checkGitHubRSSFeed();
        } catch (Exception e) {
            logger.debug("GitHub RSS feed check failed: %s".formatted(e.getMessage()));
        }
        
        // Method 3: Try direct GitHub releases page parsing as last resort
        try {
            return checkGitHubReleasesPage();
        } catch (Exception e) {
            logger.debug("GitHub releases page check failed: %s".formatted(e.getMessage()));
        }
        
        return null;
    }

    private UpdateInfo checkGitHubAPI() throws Exception {
        var connection = new URL("https://api.github.com/repos/Xreatlabs/NexAuth/releases").openConnection();
        connection.setRequestProperty("User-Agent", "NexAuth/%s".formatted(getVersion()));
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        try (var in = connection.getInputStream()) {
            var root = GSON.fromJson(new InputStreamReader(in), JsonArray.class);
            
            List<Release> releases = new ArrayList<>();
            SemanticVersion latest = null;

            for (JsonElement raw : root) {
                var release = raw.getAsJsonObject();
                
                // Skip pre-releases and drafts
                if (release.get("prerelease").getAsBoolean() || release.get("draft").getAsBoolean()) {
                    continue;
                }
                
                var version = SemanticVersion.parse(release.get("tag_name").getAsString());
                var name = release.get("name").getAsString();
                
                releases.add(new Release(version, name));
                
                if (latest == null) {
                    latest = version;
                }
            }

            return new UpdateInfo(latest, releases);
        }
    }

    private UpdateInfo checkGitHubRSSFeed() throws Exception {
        var connection = new URL("https://github.com/Xreatlabs/NexAuth/releases.atom").openConnection();
        connection.setRequestProperty("User-Agent", "NexAuth/%s".formatted(getVersion()));
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        try (var in = connection.getInputStream()) {
            var content = new String(in.readAllBytes());
            
            // Simple regex parsing for RSS feed
            var pattern = java.util.regex.Pattern.compile("<title>([^<]+)</title>");
            var matcher = pattern.matcher(content);
            
            List<Release> releases = new ArrayList<>();
            SemanticVersion latest = null;
            
            while (matcher.find()) {
                var title = matcher.group(1);
                if (title.contains("NexAuth") && !title.equals("Release notes from NexAuth")) {
                    try {
                        // Extract version from title
                        var versionPattern = java.util.regex.Pattern.compile("v?([0-9]+\\.[0-9]+\\.[0-9]+(?:-[a-zA-Z0-9]+)?)");
                        var versionMatcher = versionPattern.matcher(title);
                        
                        if (versionMatcher.find()) {
                            var version = SemanticVersion.parse(versionMatcher.group(1));
                            releases.add(new Release(version, title));
                            
                            if (latest == null) {
                                latest = version;
                            }
                        }
                    } catch (Exception e) {
                        // Skip invalid versions
                    }
                }
            }
            
            if (latest != null) {
                return new UpdateInfo(latest, releases);
            }
        }
        
        throw new Exception("No valid releases found in RSS feed");
    }

    private UpdateInfo checkGitHubReleasesPage() throws Exception {
        var connection = new URL("https://github.com/Xreatlabs/NexAuth/releases").openConnection();
        connection.setRequestProperty("User-Agent", "NexAuth/%s".formatted(getVersion()));
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);

        try (var in = connection.getInputStream()) {
            var content = new String(in.readAllBytes());
            
            // Parse HTML for release tags
            var pattern = java.util.regex.Pattern.compile("href=\"/Xreatlabs/NexAuth/releases/tag/([^\"]+)\"");
            var matcher = pattern.matcher(content);
            
            List<Release> releases = new ArrayList<>();
            SemanticVersion latest = null;
            
            while (matcher.find()) {
                try {
                    var tagName = matcher.group(1);
                    var version = SemanticVersion.parse(tagName);
                    releases.add(new Release(version, "NexAuth " + tagName));
                    
                    if (latest == null) {
                        latest = version;
                    }
                } catch (Exception e) {
                    // Skip invalid versions
                }
            }
            
            if (latest != null) {
                return new UpdateInfo(latest, releases);
            }
        }
        
        throw new Exception("No valid releases found on releases page");
    }

    private record UpdateInfo(SemanticVersion latest, List<Release> allReleases) {
    }

    public UUID generateNewUUID(String name, @Nullable UUID premiumID) {
        return switch (configuration.getNewUUIDCreator()) {
            case RANDOM -> UUID.randomUUID();
            case MOJANG -> premiumID == null ? GeneralUtil.getCrackedUUIDFromName(name) : premiumID;
            case CRACKED -> GeneralUtil.getCrackedUUIDFromName(name);
        };
    }

    protected void disable() {
        if (databaseConnector != null) {
            try {
                databaseConnector.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Failed to disconnect from database, ignoring...");
            }
        }
        if (luckpermsApi != null) {
            luckpermsApi.disable();
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public HoconPluginConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public HoconMessages getMessages() {
        return messages;
    }

    @Override
    public void checkDataFolder() {
        var folder = getDataFolder();

        if (!folder.exists()) if (!folder.mkdir()) throw new RuntimeException("Failed to create datafolder");
    }

    protected abstract Logger provideLogger();

    public abstract CommandManager<?, ?, ?, ?, ?, ?> provideManager();

    public abstract P getPlayerFromIssuer(CommandIssuer issuer);

    public abstract void authorize(P player, User user, Audience audience);

    public abstract CancellableTask delay(Runnable runnable, long delayInMillis);

    public abstract CancellableTask repeat(Runnable runnable, long delayInMillis, long repeatInMillis);

    public abstract boolean pluginPresent(String pluginName);

    protected abstract AuthenticImageProjector<P, S> provideImageProjector();

    public PremiumUser getUserOrThrowICA(String username) throws InvalidCommandArgument {
        try {
            return getPremiumProvider().getUserForName(username);
        } catch (PremiumException e) {
            throw new InvalidCommandArgument(getMessages().getMessage(
                    switch (e.getIssue()) {
                        case THROTTLED -> "error-premium-throttled";
                        default -> "error-premium-unknown";
                    }
            ));
        }
    }

    protected abstract void initMetrics(CustomChart... charts);

    @Override
    public AuthenticAuthorizationProvider<P, S> getAuthorizationProvider() {
        return authorizationProvider;
    }

    @Override
    public CryptoProvider getCryptoProvider(String id) {
        return cryptoProviders.get(id);
    }

    @Override
    public void registerCryptoProvider(CryptoProvider provider) {
        cryptoProviders.put(provider.getIdentifier(), provider);
    }

    @Override
    public CryptoProvider getDefaultCryptoProvider() {
        return getCryptoProvider(configuration.get(DEFAULT_CRYPTO_PROVIDER));
    }

    @Override
    public void migrate(ReadDatabaseProvider from, WriteDatabaseProvider to) {
        logger.info("Reading data...");
        var users = from.getAllUsers();
        logger.info("Data read, inserting into database...");
        to.insertUsers(users);
    }

    @Override
    public AuthenticEventProvider<P, S> getEventProvider() {
        return eventProvider;
    }

    public LoginTryListener<P, S> getLoginTryListener() {
        return loginTryListener;
    }

    public void onExit(P player) {
        cancelOnExit.removeAll(player).forEach(CancellableTask::cancel);
        if (configuration.get(REMEMBER_LAST_SERVER)) {
            var server = platformHandle.getPlayersServerName(player);
            if (server == null) return;
            var user = databaseProvider.getByUUID(platformHandle.getUUIDForPlayer(player));
            if (user != null && !getConfiguration().get(LIMBO).contains(server)) {
                user.setLastServer(server);
                databaseProvider.updateUser(user);
            }
        }
    }

    public void cancelOnExit(CancellableTask task, P player) {
        cancelOnExit.put(player, task);
    }

    public boolean floodgateEnabled() {
        return floodgateApi != null;
    }

    public boolean luckpermsEnabled() {
        return luckpermsApi != null;
    }

    public boolean fromFloodgate(UUID uuid) {
        return floodgateApi != null && uuid != null && floodgateApi.isFloodgateId(uuid);
    }

    protected void shutdownProxy(int code) {
        //noinspection finally
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        } finally {
            System.exit(code);
        }
    }

    public abstract Audience getAudienceFromIssuer(CommandIssuer issuer);

    protected boolean mainThread() {
        return false;
    }

    public void reportMainThread() {
        if (mainThread()) {
            logger.error("AN IO OPERATION IS BEING PERFORMED ON THE MAIN THREAD! THIS IS A SERIOUS BUG!, PLEASE REPORT IT TO THE DEVELOPER OF THE PLUGIN AND ATTACH THE STACKTRACE BELOW!");
            new Throwable().printStackTrace();
        }
    }

    public boolean fromFloodgate(String username) {
        return floodgateApi != null && floodgateApi.getPlayer(username) != null;
    }

    protected java.util.logging.Logger getSimpleLogger() {
        return null;
    }
}