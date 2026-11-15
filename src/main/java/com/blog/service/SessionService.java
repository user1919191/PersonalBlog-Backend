package main.java.com.blog.service;

import com.blog.dto.SessionUser;
import com.blog.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private static final String SESSION_PREFIX = "session:user:";
    private static final String SESSION_INDEX_PREFIX = "session:user:index:";
    private static final long SESSION_TTL_DAYS = 7;
    private static final long DIAGNOSTIC_SESSION_TTL_MINUTES = 10;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RedisTemplate<String, Object> redisTemplate;

    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String createSession(User user, String ip, String userAgent) {
        invalidateAllSessions(user.getId());
        return createSessionInternal(user, ip, userAgent, SESSION_TTL_DAYS, TimeUnit.DAYS, true);
    }

    public String createDiagnosticSession(User user, String ip, String userAgent) {
        return createSessionInternal(user, ip, userAgent, DIAGNOSTIC_SESSION_TTL_MINUTES, TimeUnit.MINUTES, false);
    }

    public Optional<SessionUser> getSession(String sessionId, String currentIp) {
        Object value = redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
        if (value == null) {
            return Optional.empty();
        }

        SessionUser session = (SessionUser) value;
        String now = LocalDateTime.now().format(FMT);

        if (currentIp != null && !Objects.equals(session.getLastIp(), currentIp)) {
            log.warn("[Session] IP changed: userId={} {} -> {}", session.getUserId(), session.getLastIp(), currentIp);
            session.setLastIp(currentIp);
        }
        session.setLastUsedAt(now);

        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, session, SESSION_TTL_DAYS, TimeUnit.DAYS);
        return Optional.of(session);
    }

    public void deleteSession(String sessionId) {
        Object value = redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
        if (value != null) {
            SessionUser session = (SessionUser) value;
            redisTemplate.opsForSet().remove(SESSION_INDEX_PREFIX + session.getUserId(), sessionId);
        }
        redisTemplate.delete(SESSION_PREFIX + sessionId);
    }

    public void invalidateAllSessions(Long userId) {
        String indexKey = SESSION_INDEX_PREFIX + userId;
        Set<Object> sessionIds = redisTemplate.opsForSet().members(indexKey);
        if (sessionIds != null) {
            for (Object sid : sessionIds) {
                redisTemplate.delete(SESSION_PREFIX + sid.toString());
            }
        }
        redisTemplate.delete(indexKey);
    }

    public long getSessionCount(Long userId) {
        Long count = redisTemplate.opsForSet().size(SESSION_INDEX_PREFIX + userId);
        return count != null ? count : 0;
    }

    private String createSessionInternal(User user,
                                         String ip,
                                         String userAgent,
                                         long ttl,
                                         TimeUnit ttlUnit,
                                         boolean extendIndexTtl) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String now = LocalDateTime.now().format(FMT);

        SessionUser sessionUser = new SessionUser(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getDisplayName(),
                user.getAvatar(),
                ip,
                ip,
                userAgent,
                now,
                now
        );

        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, sessionUser, ttl, ttlUnit);

        String indexKey = SESSION_INDEX_PREFIX + user.getId();
        redisTemplate.opsForSet().add(indexKey, sessionId);
        if (extendIndexTtl) {
            redisTemplate.expire(indexKey, SESSION_TTL_DAYS + 1, TimeUnit.DAYS);
        } else {
            redisTemplate.expire(indexKey, DIAGNOSTIC_SESSION_TTL_MINUTES + 1, TimeUnit.MINUTES);
        }

        return sessionId;
    }
}