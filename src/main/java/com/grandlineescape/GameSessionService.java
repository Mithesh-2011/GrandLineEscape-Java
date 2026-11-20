package com.grandlineescape;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GameSessionService {
    private static final Logger logger = LoggerFactory.getLogger(GameSessionService.class);
    private static final long SESSION_LIFETIME_MILLIS = TimeUnit.HOURS.toMillis(6);

    private final ConcurrentMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 16)
    public void tickSessions() {
        for (GameSession session : sessions.values()) {
            try {
                session.engine.tick(1.0 / 60.0);
            } catch (RuntimeException exception) {
                logger.error("Game session tick failed", exception);
            }
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 5 * 60 * 1000)
    public void removeExpiredSessions() {
        long cutoff = System.currentTimeMillis() - SESSION_LIFETIME_MILLIS;
        sessions.entrySet().removeIf(entry -> entry.getValue().createdAtMillis < cutoff);
    }

    public Session resolveSession(String sessionId) {
        if (sessionId != null && sessionId.matches("[0-9a-fA-F-]{36}")) {
            GameSession session = sessions.get(sessionId);
            if (session != null) {
                return new Session(sessionId, session.engine, false);
            }
        }

        String newSessionId = UUID.randomUUID().toString();
        GameSession session = new GameSession();
        sessions.put(newSessionId, session);
        return new Session(newSessionId, session.engine, true);
    }

    public static final class Session {
        private final String id;
        private final GameEngine engine;
        private final boolean created;

        private Session(String id, GameEngine engine, boolean created) {
            this.id = id;
            this.engine = engine;
            this.created = created;
        }

        public String getId() {
            return id;
        }

        public GameEngine getEngine() {
            return engine;
        }

        public boolean isCreated() {
            return created;
        }
    }

    private static final class GameSession {
        private final GameEngine engine = new GameEngine();
        private final long createdAtMillis = System.currentTimeMillis();
    }
}
