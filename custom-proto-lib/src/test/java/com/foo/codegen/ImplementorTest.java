package com.foo.codegen;


import com.foo.protoPackage.ClickEventOuterClass;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImplementorTest extends BaseCodeGenTest {

    @Test
    public void testInputEventImplements() {
        Instant now = Instant.now();
        Timestamp tsNow =
                Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        ClickEventOuterClass.ClickEvent clickEvent =
                ClickEventOuterClass.ClickEvent.newBuilder()
                        .setListing("b1")
                        .setClickedAt(tsNow)
                        .build();
        assertEquals("b1", clickEvent.getListing());

        assertEquals(ClickEventOuterClass.ClickEvent.getDefaultInstance(), ClickEventOuterClass.ClickEvent.getDefaultInstance());

        assertEquals(now, clickEvent.eventTime());
        assertEquals(tsNow, clickEvent.getClickedAt());
    }
}
