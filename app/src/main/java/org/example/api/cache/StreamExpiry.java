package org.example.api.cache;

import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.Stream;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StreamExpiry {
    private static final Pattern EXPIRE = Pattern.compile("[?&]expire=(\\d+)");

    private StreamExpiry() {}

    public static Optional<Instant> earliest(StreamInfo info) {
        List<Stream> all = new ArrayList<>();
        all.addAll(info.getVideoOnlyStreams());
        all.addAll(info.getAudioStreams());
        all.addAll(info.getVideoStreams());

        long earliest = Long.MAX_VALUE;
        for (var stream : all) {
            String content = stream.getContent();
            Matcher matcher = EXPIRE.matcher(content);
            if (matcher.find()) {
                earliest = Math.min(earliest, Long.parseLong(matcher.group(1)));
            }
        }
        return earliest == Long.MAX_VALUE ? Optional.empty() : Optional.of(Instant.ofEpochSecond(earliest));
    }
}
