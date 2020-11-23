package com.foo;

import java.time.Instant;

public interface KafkaEvent<T> {
    Instant eventTime();
}
