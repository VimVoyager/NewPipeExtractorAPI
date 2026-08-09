package org.example.api.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.example.api.exception.ExtractionException;
import org.jspecify.annotations.NonNull;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletionException;

@Component
public class StreamInfoCache {
    private final Cache<String, StreamInfo> cache;

    public StreamInfoCache(StreamInfoCacheProperties properties) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(properties.maxEntries())
                .expireAfter(new Expiry<String, StreamInfo>() {
                    @Override
                    public long expireAfterCreate(@NonNull String key, @NonNull StreamInfo value, long currentTime) {
                        return ttlNanos(value, properties);
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull String key, @NonNull StreamInfo value, long currentTime, long currentDuration) {
                        return ttlNanos(value, properties);
                    }

                    @Override
                    public long expireAfterRead(@NonNull String key, @NonNull StreamInfo value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .recordStats()
                .build();
    }

    private static long ttlNanos(StreamInfo info, StreamInfoCacheProperties props) {
        boolean degraded = info.getVideoOnlyStreams().isEmpty() && info.getAudioStreams().isEmpty();
        if (degraded) {
            return props.degradedTtl().toNanos();
        }

        Duration ttl = StreamExpiry.earliest(info)
                .map(exp -> Duration.between(Instant.now(), exp).minus(props.safetyMargin()))
                .orElse(props.fallbackTtl());

        if (ttl.compareTo(props.maxTtl()) > 0) ttl = props.maxTtl();
        return Math.max(0L, ttl.toNanos());
    }

    public StreamInfo get(String url, ExtractionLoader loader) throws ExtractionException {
        try {
            return cache.get(url, key -> {
                try {
                    return loader.load(key);
                } catch (ExtractionException e) {
                    throw new CompletionException(e);
                }
            });
        } catch (CompletionException e) {
            if (e.getCause() instanceof ExtractionException ex) throw ex;
            throw e;
        }
    }

    public void invalidate(String url) { cache.invalidate(url); }
    public CacheStats stats() { return cache.stats(); }

    @FunctionalInterface
    public interface ExtractionLoader {
        StreamInfo load(String url) throws ExtractionException;
    }
}
