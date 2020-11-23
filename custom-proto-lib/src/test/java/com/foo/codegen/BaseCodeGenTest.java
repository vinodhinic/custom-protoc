package com.foo.codegen;

import com.google.protobuf.Timestamp;

public abstract class BaseCodeGenTest {
    protected java.util.function.Function<Timestamp, java.time.Instant>
            timestampToInstant =
            (t) -> {
                if (t == null) {
                    t = Timestamp.getDefaultInstance();
                }
                return java.time.Instant.ofEpochSecond(t.getSeconds(), t.getNanos());
            };

    protected java.util.function.Function<java.time.Instant, Timestamp>
            instantToTimestamp =
            (i) -> {
                if (i == null) {
                    return Timestamp.getDefaultInstance();
                }
                return Timestamp.newBuilder()
                        .setSeconds(i.getEpochSecond())
                        .setNanos(i.getNano())
                        .build();
            };
}
