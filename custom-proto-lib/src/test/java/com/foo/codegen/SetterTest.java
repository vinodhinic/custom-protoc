package com.foo.codegen;

import com.foo.javaPackage.CompProto;
import com.foo.javaPackage.ManyMessagesWithJavaPackage;
import com.foo.protoPackage.ManyMessagesWithProtoPackage;
import com.foo.protoPackage.TestSnapCreation;
import com.foo.types.LocalDateProto;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SetterTest extends BaseCodeGenTest {

    @Test
    public void testInstantSetter() {
        Instant now = Instant.now();
        Timestamp tsNow =
                Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        TestSnapCreation sod = TestSnapCreation.newBuilder().setInstantCreatedAt(now).build();

        assertTrue(sod.hasCreatedAt());
        assertEquals(tsNow, sod.getCreatedAt());
        assertTrue(sod.hasInstantCreatedAt());
        assertEquals(now, sod.getInstantCreatedAt());

        sod = sod.toBuilder().setInstantCreatedAt(null).build();
        assertTrue(sod.hasCreatedAt());
        assertTrue(sod.hasInstantCreatedAt());
        // This is important. if you set an auxilliary field to null, you are not going to get back a
        // null.
        assertEquals(
                timestampToInstant.apply(Timestamp.getDefaultInstance()), sod.getInstantCreatedAt());
        assertEquals(Timestamp.getDefaultInstance(), sod.getCreatedAt());
    }

    @Test
    public void testLocalDate() {
        LocalDate ld = LocalDate.of(1992, 3, 7);
        ManyMessagesWithJavaPackage.Birth birth =
                ManyMessagesWithJavaPackage.Birth.newBuilder()
                        .setBirthDate(
                                LocalDateProto.LocalDate.newBuilder()
                                        .setYear(1992)
                                        .setMonth(7)
                                        .setDayOfMonth(7)
                                        .build())
                        .setLocalDateBirthDate(ld) /* latest setter wins */
                        .build();
        assertTrue(birth.hasBirthDate());
        assertTrue(birth.hasLocalDateBirthDate());
        assertEquals(ld, birth.getLocalDateBirthDate());
        assertEquals(
                LocalDateProto.LocalDate.newBuilder()
                        .setDayOfMonth(ld.getDayOfMonth())
                        .setMonth(ld.getMonthValue())
                        .setYear(ld.getYear())
                        .build(),
                birth.getBirthDate());
    }

    @Test
    public void testBigDecimal() {
        BigDecimal td = new BigDecimal("10.9091");
        CompProto.TestComp testBond =
                CompProto.TestComp.newBuilder().setBigDecimalIncrement(td).build();
        assertTrue(testBond.hasBigDecimalIncrement());
        assertTrue(testBond.getIncrement().contains("10.9091"));
        assertEquals(td.compareTo(new BigDecimal(testBond.getIncrement())), 0);
        assertEquals(td.compareTo(testBond.getBigDecimalIncrement()), 0);

        ManyMessagesWithProtoPackage.TestOrderPrice testRealizedGain =
                ManyMessagesWithProtoPackage.TestOrderPrice.newBuilder().setBigDecimalOriginalPrice(td).build();
        assertTrue(testRealizedGain.hasBigDecimalOriginalPrice());
        assertTrue(testRealizedGain.getPrice().contains("10.9091"));
        assertEquals(td.compareTo(new BigDecimal(testRealizedGain.getPrice())), 0);
        assertEquals(td.compareTo(testRealizedGain.getBigDecimalOriginalPrice()), 0);
    }
}
