import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public class GameServer {
    private static final long SESSION_LIFETIME_MILLIS = TimeUnit.HOURS.toMillis(6);

    private final int port;
    private final ConcurrentMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    private HttpServer server;
    private ScheduledExecutorService ticker;
    private ScheduledExecutorService sessionCleaner;
    private ExecutorService requestExecutor;

    public GameServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", new StaticHtmlHandler());

        server.createContext("/health", new HealthHandler());
        server.createContext("/api/state", new StateHandler());
        server.createContext("/api/start", new StartHandler());
        server.createContext("/api/input", new InputHandler());
        server.createContext("/api/action", new ActionHandler());

        requestExecutor = Executors.newFixedThreadPool(16);
        server.setExecutor(requestExecutor);
        server.start();

        ticker = Executors.newSingleThreadScheduledExecutor();
        ticker.scheduleAtFixedRate(() -> {
            for (GameSession session : sessions.values()) {
                try {
                    session.engine.tick(1.0 / 60.0);
                } catch (RuntimeException ex) {
                    System.err.println("Game session tick failed: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }, 0, 16, TimeUnit.MILLISECONDS);

        sessionCleaner = Executors.newSingleThreadScheduledExecutor();
        sessionCleaner.scheduleAtFixedRate(this::removeExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }

    public void stop() {
        if (server != null) {
            server.stop(1);
        }
        if (ticker != null) {
            ticker.shutdownNow();
        }
        if (sessionCleaner != null) {
            sessionCleaner.shutdownNow();
        }
        if (requestExecutor != null) {
            requestExecutor.shutdown();
        }
    }

    private void removeExpiredSessions() {
        long cutoff = System.currentTimeMillis() - SESSION_LIFETIME_MILLIS;
        sessions.entrySet().removeIf(entry -> entry.getValue().createdAtMillis < cutoff);
    }

    private GameEngine getSessionEngine(HttpExchange exchange) {
        String sessionId = readSessionCookie(exchange);
        GameSession session = sessionId == null ? null : sessions.get(sessionId);
        if (session != null) {
            return session.engine;
        }

        String newSessionId = UUID.randomUUID().toString();
        session = new GameSession(new GameEngine());
        sessions.put(newSessionId, session);
        String forwardedProto = exchange.getRequestHeaders().getFirst("X-Forwarded-Proto");
        boolean secure = forwardedProto != null && forwardedProto.split(",", 2)[0].trim().equalsIgnoreCase("https");
        String cookie = "GAME_SESSION=" + newSessionId + "; Path=/; Max-Age="
                + TimeUnit.MILLISECONDS.toSeconds(SESSION_LIFETIME_MILLIS) + "; HttpOnly; SameSite=Lax";
        exchange.getResponseHeaders().add("Set-Cookie", secure ? cookie + "; Secure" : cookie);
        return session.engine;
    }

    private String readSessionCookie(HttpExchange exchange) {
        for (String header : exchange.getRequestHeaders().getOrDefault("Cookie", java.util.Collections.emptyList())) {
            for (String cookie : header.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && "GAME_SESSION".equals(parts[0])
                        && parts[1].matches("[0-9a-fA-F-]{36}")) {
                    return parts[1];
                }
            }
        }
        return null;
    }

    private class StaticHtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            getSessionEngine(exchange);
            File htmlFile = new File("web/index.html");
            if (!htmlFile.exists()) {
                htmlFile = new File("../web/index.html");
            }
            if (!htmlFile.exists()) {
                byte[] msg = "Missing web/index.html".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, msg.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(msg);
                }
                return;
            }
            byte[] bytes = readAllBytes(htmlFile);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            sendJson(exchange, 200, "{\"status\":\"ok\"}");
        }
    }

    private class StateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            sendJson(exchange, 200, getSessionEngine(exchange).toJsonSnapshot());
        }
    }

    private class StartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            String difficulty = extractJsonString(body, "difficulty", "Normal");
            String callsign = extractJsonString(body, "callsign", "Monkey D. Luffy");
            GameEngine engine = getSessionEngine(exchange);
            engine.resetGame(difficulty, callsign);
            sendJson(exchange, 200, engine.toJsonSnapshot());
        }
    }

    private class InputHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            boolean up = extractJsonBoolean(body, "up");
            boolean down = extractJsonBoolean(body, "down");
            boolean left = extractJsonBoolean(body, "left");
            boolean right = extractJsonBoolean(body, "right");
            boolean firing = extractJsonBoolean(body, "firing");
            double aimAngle = extractJsonDouble(body, "aimAngle", 0.0);
            GameEngine engine = getSessionEngine(exchange);
            engine.updateInputState(up, down, left, right, firing, aimAngle);
            sendJson(exchange, 200, "{\"ok\":true}");
        }
    }

    private class ActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }
            String body = readBody(exchange);
            String action = extractJsonString(body, "action", "");
            int param = (int) Math.round(extractJsonDouble(body, "param", 0.0));
            GameEngine engine = getSessionEngine(exchange);
            String result = engine.executePlayerAction(action, param);
            sendJson(exchange, 200, "{\"result\":\"" + result + "\"}");
        }
    }

    private String extractJsonString(String json, String key, String fallback) {
        if (json == null) return fallback;
        String token = "\"" + key + "\":\"";
        int idx = json.indexOf(token);
        if (idx == -1) return fallback;
        int start = idx + token.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return fallback;
        return json.substring(start, end);
    }

    private boolean extractJsonBoolean(String json, String key) {
        if (json == null) return false;
        String token = "\"" + key + "\":true";
        return json.indexOf(token) != -1;
    }

    private double extractJsonDouble(String json, String key, double fallback) {
        if (json == null) return fallback;
        String token = "\"" + key + "\":";
        int idx = json.indexOf(token);
        if (idx == -1) return fallback;
        int start = idx + token.length();
        int end = start;
        while (end < json.length()) {
            char ch = json.charAt(end);
            if ((ch >= '0' && ch <= '9') || ch == '.' || ch == '-') {
                end++;
            } else {
                break;
            }
        }
        if (end == start) return fallback;
        try {
            return Double.parseDouble(json.substring(start, end));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }

    private byte[] readAllBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return baos.toByteArray();
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        if (acceptsGzip(exchange)) {
            ByteArrayOutputStream compressed = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
                gzip.write(bytes);
            }
            bytes = compressed.toByteArray();
            exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("Vary", "Accept-Encoding");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private boolean acceptsGzip(HttpExchange exchange) {
        String acceptEncoding = exchange.getRequestHeaders().getFirst("Accept-Encoding");
        if (acceptEncoding == null) {
            return false;
        }
        for (String encoding : acceptEncoding.split(",")) {
            String[] parts = encoding.trim().split(";", 2);
            if (!"gzip".equalsIgnoreCase(parts[0].trim())) {
                continue;
            }
            if (parts.length == 1) {
                return true;
            }
            for (String parameter : parts[1].split(";")) {
                String[] setting = parameter.trim().split("=", 2);
                if (setting.length == 2 && "q".equalsIgnoreCase(setting[0].trim())) {
                    try {
                        return Double.parseDouble(setting[1].trim()) > 0.0;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private static class GameSession {
        private final GameEngine engine;
        private final long createdAtMillis;

        private GameSession(GameEngine engine) {
            this.engine = engine;
            this.createdAtMillis = System.currentTimeMillis();
        }
    }
}
