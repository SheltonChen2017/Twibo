package edu.miis.web;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AuthenticationRateLimiter {
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int LOGIN_ACCOUNT_LIMIT = 10;
    private static final int LOGIN_ADDRESS_LIMIT = 40;
    private static final int RECOVERY_ACCOUNT_LIMIT = 5;
    private static final int RECOVERY_ADDRESS_LIMIT = 20;
    private static final int MAX_BUCKETS = 10_000;

    private final ConcurrentHashMap<String, AttemptWindow> attempts = new ConcurrentHashMap<>();
    private final AtomicInteger operations = new AtomicInteger();

    public Optional<Duration> loginRetryAfter(String username, String address) {
        return retryAfter("login", username, address, LOGIN_ACCOUNT_LIMIT, LOGIN_ADDRESS_LIMIT);
    }

    public void recordLoginFailure(String username, String address) {
        recordFailure("login", username, address);
    }

    public void recordLoginSuccess(String username) {
        attempts.remove(subjectKey("login", username));
    }

    public Optional<Duration> recoveryRetryAfter(String username, String address) {
        return retryAfter("recovery", username, address, RECOVERY_ACCOUNT_LIMIT, RECOVERY_ADDRESS_LIMIT);
    }

    public void recordRecoveryFailure(String username, String address) {
        recordFailure("recovery", username, address);
    }

    public void recordRecoverySuccess(String username) {
        attempts.remove(subjectKey("recovery", username));
    }

    private Optional<Duration> retryAfter(String action, String subject, String address,
                                          int subjectLimit, int addressLimit) {
        Instant now = Instant.now();
        cleanupOccasionally(now);
        Duration subjectWait = waitFor(subjectKey(action, subject), subjectLimit, now);
        Duration addressWait = waitFor(addressKey(action, address), addressLimit, now);
        Duration wait = subjectWait.compareTo(addressWait) >= 0 ? subjectWait : addressWait;
        return wait.isZero() ? Optional.empty() : Optional.of(wait);
    }

    private void recordFailure(String action, String subject, String address) {
        Instant now = Instant.now();
        record(addressKey(action, address), now);
        record(subjectKey(action, subject), now);
        cleanupOccasionally(now);
    }

    private void record(String key, Instant now) {
        if (!attempts.containsKey(key) && attempts.size() >= MAX_BUCKETS) return;
        attempts.compute(key, (ignored, current) -> {
            if (current == null || current.startedAt().plus(WINDOW).isBefore(now)) {
                return new AttemptWindow(1, now);
            }
            return new AttemptWindow(current.failures() + 1, current.startedAt());
        });
    }

    private Duration waitFor(String key, int limit, Instant now) {
        AttemptWindow window = attempts.get(key);
        if (window == null || window.failures() < limit) return Duration.ZERO;
        Instant expiresAt = window.startedAt().plus(WINDOW);
        if (!expiresAt.isAfter(now)) {
            attempts.remove(key, window);
            return Duration.ZERO;
        }
        return Duration.between(now, expiresAt);
    }

    private void cleanupOccasionally(Instant now) {
        if (operations.incrementAndGet() % 256 != 0 && attempts.size() < MAX_BUCKETS) return;
        Instant cutoff = now.minus(WINDOW);
        attempts.entrySet().removeIf(entry -> entry.getValue().startedAt().isBefore(cutoff));
    }

    private String subjectKey(String action, String subject) {
        String normalized = subject == null ? "" : subject.trim().toLowerCase(Locale.ROOT);
        return action + ":subject:" + normalized;
    }

    private String addressKey(String action, String address) {
        return action + ":address:" + (address == null ? "unknown" : address);
    }

    private record AttemptWindow(int failures, Instant startedAt) {}
}
