package com.mae.sysdesign.ratelimit;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class Token implements Comparable<Token> {
    private static final long DEFAULT_TOKEN_EXPIRED_DURATION_MS = 1000 * 2;
    private static AtomicLong idCounter = new AtomicLong(0);
    private String id;
    private LocalDateTime generatedTimestamp;
    private LocalDateTime expiredTimestamp;
    private Long expiredDurationInMS = null;

    public Token() {
        this(DEFAULT_TOKEN_EXPIRED_DURATION_MS);
    }

    public Token(long tokenExpiredDurationMs) {
        id = "Token-" + idCounter.getAndIncrement();
        generatedTimestamp = LocalDateTime.now();
        this.expiredDurationInMS = tokenExpiredDurationMs;
        if (expiredDurationInMS == null) {
            expiredDurationInMS = DEFAULT_TOKEN_EXPIRED_DURATION_MS;
        }
        expiredTimestamp = generatedTimestamp.plus(expiredDurationInMS, ChronoUnit.MILLIS);
    }

    @Override
    public int compareTo(Token o) {
        return this.expiredTimestamp.compareTo(o.expiredTimestamp);
    }

    public boolean isTokenExpired() {
        return LocalDateTime.now().compareTo(this.expiredTimestamp) > 0;
    }
}
