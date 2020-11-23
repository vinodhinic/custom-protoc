package com.foo.codegen;

import com.foo.types.LocalDateProto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtenderTest extends BaseCodeGenTest {

    @Test
    public void testLocalDateStaticMethods() {
        LocalDate javaLocalDate = LocalDate.of(1992, 3, 7);
        LocalDateProto.LocalDate localDate = LocalDateProto.fromJavaLocalDate(javaLocalDate);
        assertEquals(localDate, LocalDateProto.LocalDate.fromJavaLocalDate(javaLocalDate));
        assertEquals(7, localDate.getDayOfMonth());
        assertEquals(3, localDate.getMonth());
        assertEquals(1992, localDate.getYear());

        assertEquals(localDate, LocalDateProto.fromString("19920307"));

        javaLocalDate = LocalDate.now();
        assertEquals(LocalDateProto.fromJavaLocalDate(javaLocalDate), LocalDateProto.now());
    }
}
