
package me.alstepan;

import io.helidon.common.LogConfig;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.config.PollingStrategies;
import io.helidon.dbclient.DbClient;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.media.jsonb.JsonbSupport;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;
import me.alstepan.users.domain.UserRepository;
import me.alstepan.users.infra.db.UserDAO;
import me.alstepan.users.infra.endpoints.UserService;

import java.nio.file.Paths;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * The application main class.
 */
public final class Server {

    /**
     * Cannot be instantiated.
     */
    private Server() {
    }

    /**
     * Application main entry point.
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        var cfgInd = Arrays.binarySearch(args,"--conf");
        var cfgPath = (cfgInd >= 0 && (cfgInd + 1) < args.length) ? args[cfgInd + 1] : "";
        Logger.getLogger(Server.class.getName()).info("Optional config path is " + cfgPath);
        startServer(cfgPath);
    }

    /**
     * Start the server.
     * @return the created {@link WebServer} instance
     */
    static Single<WebServer> startServer(String cfgPath) {

        // load logging configuration
        LogConfig.configureRuntime();

        // Loading config either from classpath or from external file
        var config = Config
                .builder()
                .sources(
                        ConfigSources
                                .classpath("application.yaml")
                                .optional(),
                        ConfigSources
                                .file(Paths.get(cfgPath, "application.yaml"))
                                .optional()
                )
                .build();

        WebServer server = WebServer.builder(createRouting(config))
                .config(config.get("server"))
                .addMediaSupport(JsonpSupport.create())
                .addMediaSupport(JsonbSupport.create())
                .build();

        Single<WebServer> webserver = server.start();

        // Try to start the server. If successful, print some info and arrange to
        // print a message at shutdown. If unsuccessful, print the exception.
        webserver.thenAccept(ws -> {
                    System.out.println("WEB server is up! http://localhost:" + ws.port());
                    ws.whenShutdown().thenRun(() -> System.out.println("WEB server is DOWN. Good bye!"));
                })
                .exceptionallyAccept(t -> {
                    System.err.println("Startup failed: " + t.getMessage());
                    t.printStackTrace(System.err);
                });

        return webserver;
    }

    /**
     * Creates new {@link Routing}.
     *
     * @return routing configured with JSON support, a health check, and a service
     * @param config configuration of this server
     */
    private static Routing createRouting(Config config) {

        MetricsSupport metrics = MetricsSupport.create();
        Config dbConfig = config.get("db");
        DbClient client = DbClient.create(dbConfig);
        UserRepository repo = new UserDAO(client);
        UserService userService = new UserService(repo);
        HealthSupport health = HealthSupport.builder()
                .addLiveness(HealthChecks.healthChecks())   // Adds a convenient set of checks
                .build();

        return Routing.builder()
                .register(health)                   // Health at "/health"
                .register(metrics)                  // Metrics at "/metrics"
                .register("/user", userService)
                .build();
    }
}
