package com.grandlineescape;

import java.time.Duration;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {
    private static final String SESSION_COOKIE = "GAME_SESSION";
    private final GameSessionService sessionService;

    public GameController(GameSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> index(HttpServletRequest request, HttpServletResponse response) {
        resolveSession(request, response);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(new ClassPathResource("static/index.html"));
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping(value = "/api/state", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> state(HttpServletRequest request, HttpServletResponse response) {
        GameEngine engine = resolveSession(request, response);
        return json(engine.toJsonSnapshot());
    }

    @PostMapping(value = "/api/start", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> start(@RequestBody(required = false) StartRequest body,
                                        HttpServletRequest request, HttpServletResponse response) {
        String difficulty = body == null || body.difficulty() == null ? "Normal" : body.difficulty();
        String callsign = body == null || body.callsign() == null ? "Monkey D. Luffy" : body.callsign();
        GameEngine engine = resolveSession(request, response);
        engine.resetGame(difficulty, callsign);
        return json(engine.toJsonSnapshot());
    }

    @PostMapping(value = "/api/input", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> input(@RequestBody(required = false) InputRequest body,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        GameEngine engine = resolveSession(request, response);
        InputRequest input = body == null ? InputRequest.empty() : body;
        double aimAngle = input.aimAngle() == null ? 0.0 : input.aimAngle();
        engine.updateInputState(input.up(), input.down(), input.left(), input.right(),
                input.firing(), aimAngle);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of("ok", true));
    }

    @PostMapping(value = "/api/action", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> action(@RequestBody(required = false) ActionRequest body,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        GameEngine engine = resolveSession(request, response);
        String action = body == null || body.action() == null ? "" : body.action();
        double rawParam = body == null || body.param() == null ? 0.0 : body.param();
        String result = engine.executePlayerAction(action, (int) Math.round(rawParam));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of("result", result));
    }

    private GameEngine resolveSession(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SESSION_COOKIE.equals(cookie.getName())
                        && cookie.getValue().matches("[0-9a-fA-F-]{36}")) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }

        GameSessionService.Session session = sessionService.resolveSession(sessionId);
        if (session.isCreated()) {
            response.addHeader(HttpHeaders.SET_COOKIE, createSessionCookie(request, session.getId()));
        }
        return session.getEngine();
    }

    private String createSessionCookie(HttpServletRequest request, String sessionId) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        boolean secure = request.isSecure()
                || forwardedProto != null
                && forwardedProto.split(",", 2)[0].trim().equalsIgnoreCase("https");
        return ResponseCookie.from(SESSION_COOKIE, sessionId)
                .path("/")
                .maxAge(Duration.ofHours(6))
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .build()
                .toString();
    }

    private ResponseEntity<String> json(String body) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    public record StartRequest(String difficulty, String callsign) {}

    public record InputRequest(boolean up, boolean down, boolean left, boolean right,
                               boolean firing, Double aimAngle) {
        private static InputRequest empty() {
            return new InputRequest(false, false, false, false, false, 0.0);
        }
    }

    public record ActionRequest(String action, Double param) {}
}
