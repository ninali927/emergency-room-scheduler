package app;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

/**
 * Main application class that sets up the web server and WebSocket connections
 * for the Emergency Room Scheduler visualization.
 */
public class Application {
    private static final int PORT = 7001;
    private static final Gson gson = new Gson();

    // Store WebSocket connections for real-time updates
    private static final Map<String, WsContext> connections = new ConcurrentHashMap<>();

    // Store ER controllers per session
    private static final Map<String, ERController> erControllers = new ConcurrentHashMap<>();

    // Store Visualization controllers per session
    private static final Map<String, VisualizationController> vizControllers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // Serve static files from frontend directory
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "../frontend";
                staticFiles.location = io.javalin.http.staticfiles.Location.EXTERNAL;
            });

            // Enable CORS for development - Javalin 6.x way
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(corsRule -> corsRule.anyHost());
            });
        }).start(PORT);

        // Serve the main page
        app.get("/", ctx -> ctx.redirect("/index.html"));

        // WebSocket endpoint for real-time ER system visualization
        app.ws("/er", ws -> {
            ws.onConnect(ctx -> {
                String sessionId = ctx.sessionId();
                connections.put(sessionId, ctx);

                System.out.println("Client connected: " + sessionId);

                // Initialize ER controller for this session
                erControllers.put(sessionId, new ERController());

                // Send initial state
                AlgorithmMessage welcome = new AlgorithmMessage("connected", "ER Scheduler ready");
                welcome.data = Map.of(
                    "serverInfo", "Emergency Room Scheduler Backend",
                    "version", "1.0",
                    "features", Map.of("registry", true, "queue", true)
                );
                ctx.send(gson.toJson(welcome));
            });

            ws.onMessage(ctx -> {
                try {
                    AlgorithmMessage message = gson.fromJson(ctx.message(), AlgorithmMessage.class);
                    handleERMessage(ctx, message);
                } catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage());
                    e.printStackTrace();
                    ctx.send(gson.toJson(new AlgorithmMessage("error", "Invalid message format: " + e.getMessage())));
                }
            });

            ws.onClose(ctx -> {
                String sessionId = ctx.sessionId();
                connections.remove(sessionId);
                erControllers.remove(sessionId);
                System.out.println("Client disconnected: " + sessionId);
            });
        });

        // WebSocket endpoint for AVL tree visualization
        app.ws("/visualization", ws -> {
            ws.onConnect(ctx -> {
                String sessionId = ctx.sessionId();
                connections.put(sessionId, ctx);

                System.out.println("Visualization client connected: " + sessionId);

                // Initialize Visualization controller for this session
                vizControllers.put(sessionId, new VisualizationController());

                // Send initial state
                AlgorithmMessage welcome = new AlgorithmMessage("viz_connected", "AVL Visualization ready");
                welcome.data = Map.of(
                    "serverInfo", "AVL Tree Visualization Backend",
                    "version", "1.0",
                    "features", Map.of("snapshots", true, "replay", true)
                );
                ctx.send(gson.toJson(welcome));
            });

            ws.onMessage(ctx -> {
                try {
                    AlgorithmMessage message = gson.fromJson(ctx.message(), AlgorithmMessage.class);
                    handleVisualizationMessage(ctx, message);
                } catch (Exception e) {
                    System.err.println("Error processing visualization message: " + e.getMessage());
                    e.printStackTrace();
                    ctx.send(gson.toJson(new AlgorithmMessage("error", "Invalid message format: " + e.getMessage())));
                }
            });

            ws.onClose(ctx -> {
                String sessionId = ctx.sessionId();
                connections.remove(sessionId);
                vizControllers.remove(sessionId);
                System.out.println("Visualization client disconnected: " + sessionId);
            });
        });

        // Start periodic ping to all clients every 30 seconds
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            AlgorithmMessage ping = new AlgorithmMessage("ping", "keepalive");
            ping.timestamp = System.currentTimeMillis();
            String json = gson.toJson(ping);
            connections.values().forEach(ctx -> {
                if (ctx.session.isOpen()) {
                    ctx.send(json);
                }
            });
        }, 30, 30, TimeUnit.SECONDS);

        System.out.println("Server started on http://localhost:" + PORT);
        System.out.println("ER Scheduler Visualizer - Open your browser to view the application");
    }

    /**
     * Handle incoming WebSocket messages from the frontend.
     * Routes messages to the appropriate ER controller.
     */
    private static void handleERMessage(WsContext ctx, AlgorithmMessage message) {
        String sessionId = ctx.sessionId();

        ERController controller = erControllers.get(sessionId);
        if (controller == null) {
            ctx.send(gson.toJson(new AlgorithmMessage("error", "No ER controller found for session")));
            return;
        }

        // Handle ping/pong
        if ("pong".equals(message.type)) {
            System.out.println("Received pong from client: " + sessionId);
            return;
        }

        // Route to ER controller
        controller.handleMessage(ctx, message);
    }

    /**
     * Handle incoming WebSocket messages for visualization.
     * Routes messages to the appropriate Visualization controller.
     */
    private static void handleVisualizationMessage(WsContext ctx, AlgorithmMessage message) {
        String sessionId = ctx.sessionId();

        VisualizationController controller = vizControllers.get(sessionId);
        if (controller == null) {
            ctx.send(gson.toJson(new AlgorithmMessage("error", "No visualization controller found for session")));
            return;
        }

        // Handle ping/pong
        if ("pong".equals(message.type)) {
            System.out.println("Received pong from visualization client: " + sessionId);
            return;
        }

        // Route to Visualization controller
        controller.handleMessage(ctx, message);
    }

    /**
     * Get the Gson instance used for JSON serialization.
     */
    public static Gson getGson() {
        return gson;
    }
}
