package com.foo.codegen;

import com.foo.javaPackage.*;
import com.foo.protoPackage.*;
import com.foo.types.LocalDateProto;
import com.google.protobuf.StringValue;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class AccessorTest extends BaseCodeGenTest {

    @Test
    public void testSimpleProto() {
        SimpleHelloWorld.TestHello hello =
                SimpleHelloWorld.TestHello.newBuilder().setGreetings("Hello").build();
        Assertions.assertEquals("Hello", hello.getGreetings());
    }

    @Test
    public void testWhereProtoMessageNameIsSameAsFileNameWithNoOuterClassJavaAnnotation() {
        MessageSameAsFileNameOuterClass.MessageSameAsFileName messageSameAsFileName =
                MessageSameAsFileNameOuterClass.MessageSameAsFileName.newBuilder().build();
        Assertions.assertFalse(messageSameAsFileName.hasBirthDate());
        Assertions.assertNull(messageSameAsFileName.getLocalDateBirthDate());
    }

    @Test
    public void testSingleMessageWithProtoPackage() {
        String decimal = "90.018";
        SingleWithProtoPackage.TestOrder order =
                SingleWithProtoPackage.TestOrder.newBuilder()
                        .setFinalPrice(StringValue.newBuilder().setValue(decimal).build())
                        .build();
        Assertions.assertEquals(order.getFinalPrice().getValue(), decimal);
        Assertions.assertTrue(order.hasBigDecimalFinalPrice());
        Assertions.assertEquals(new BigDecimal(decimal).compareTo(order.getBigDecimalFinalPrice()), 0);

        order =
                SingleWithProtoPackage.TestOrder.newBuilder()
                        .setFinalPrice(StringValue.newBuilder().setValue("").build())
                        .build();
        Assertions.assertFalse(order.hasBigDecimalFinalPrice());
        Assertions.assertNull(order.getBigDecimalFinalPrice());

        order =
                SingleWithProtoPackage.TestOrder.newBuilder()
                        .setFinalPrice(StringValue.newBuilder().build())
                        .build();
        Assertions.assertFalse(order.hasBigDecimalFinalPrice());
        Assertions.assertNull(order.getBigDecimalFinalPrice());

        order = SingleWithProtoPackage.TestOrder.newBuilder().build();
        Assertions.assertFalse(order.hasBigDecimalFinalPrice());
        Assertions.assertNull(order.getBigDecimalFinalPrice());
    }

    @Test
    public void testSingleMessageWithJavaPackage() {
        String priceStr = "90.018";
        SingleWithJavaPackage.TestListing price =
                SingleWithJavaPackage.TestListing.newBuilder().setPrice(priceStr).build();
        Assertions.assertEquals(priceStr, price.getPrice());
        Assertions.assertTrue(price.hasBigDecimalPrice());
        Assertions.assertEquals(
                0, new BigDecimal(priceStr).compareTo(price.getBigDecimalPrice()));

        price = SingleWithJavaPackage.TestListing.newBuilder().setPrice("").build();
        Assertions.assertFalse(price.hasBigDecimalPrice());
        Assertions.assertNull(price.getBigDecimalPrice());

        price = SingleWithJavaPackage.TestListing.newBuilder().build();
        Assertions.assertFalse(price.hasBigDecimalPrice());
        Assertions.assertNull(price.getBigDecimalPrice());
    }

    @Test
    public void testSingleMessageWithJavaOuterClass() {
        String rateStr = "93.9837";
        CompProto.TestComp comp = CompProto.TestComp.newBuilder().setIncrement(rateStr).build();
        Assertions.assertTrue(comp.hasBigDecimalIncrement());
        Assertions.assertEquals(rateStr, comp.getIncrement());
        Assertions.assertEquals(
                0, new BigDecimal(rateStr).compareTo(comp.getBigDecimalIncrement()));

        comp = CompProto.TestComp.newBuilder().build();
        Assertions.assertFalse(comp.hasBigDecimalIncrement());
        Assertions.assertNull(comp.getBigDecimalIncrement());

        comp = CompProto.TestComp.newBuilder().setIncrement("").build();
        Assertions.assertFalse(comp.hasBigDecimalIncrement());
        Assertions.assertNull(comp.getBigDecimalIncrement());
    }

    @Test
    public void testManyMessagesWithProtoPackage() {
        String price1 = "82.637";
        String price2 = "7.864";
        ManyMessagesWithProtoPackage.TestPizzaOrder pnl =
                ManyMessagesWithProtoPackage.TestPizzaOrder.newBuilder()
                        .setTax(
                                ManyMessagesWithProtoPackage.TestTax.newBuilder()
                                        .setPrice(price1)
                                        .build())
                        .setPrice(
                                ManyMessagesWithProtoPackage.TestOrderPrice.newBuilder()
                                        .setPrice(price2)
                                        .build())
                        .build();

        Assertions.assertEquals(price1, pnl.getTax().getPrice());
        Assertions.assertTrue(pnl.getTax().hasBigDecimalTax());
        Assertions.assertEquals(
                0,
                new BigDecimal(price1)
                        .compareTo(pnl.getTax().getBigDecimalTax()));

        Assertions.assertEquals(price2, pnl.getPrice().getPrice());
        Assertions.assertTrue(pnl.getPrice().hasBigDecimalOriginalPrice());
        Assertions.assertEquals(
                0,
                new BigDecimal(price2)
                        .compareTo(pnl.getPrice().getBigDecimalOriginalPrice()));

        pnl =
                ManyMessagesWithProtoPackage.TestPizzaOrder.newBuilder()
                        .setTax(
                                ManyMessagesWithProtoPackage.TestTax.newBuilder().setPrice("").build())
                        .build();
        Assertions.assertNull(pnl.getTax().getBigDecimalTax());
        Assertions.assertFalse(pnl.getTax().hasBigDecimalTax());
        Assertions.assertNull(pnl.getPrice().getBigDecimalOriginalPrice());
        Assertions.assertFalse(pnl.getPrice().hasBigDecimalOriginalPrice());
    }

    @Test
    public void testManyMessagesWithJavaPackage() {
        ManyMessagesWithJavaPackage.TestContract contract =
                ManyMessagesWithJavaPackage.TestContract.newBuilder()
                        .setBirth(
                                ManyMessagesWithJavaPackage.Birth.newBuilder()
                                        .setBirthDate(
                                                LocalDateProto.LocalDate.newBuilder()
                                                        .setMonth(3)
                                                        .setDayOfMonth(7)
                                                        .setYear(1992))
                                        .build())
                        .build();
        Assertions.assertTrue(contract.getBirth().hasLocalDateBirthDate());
        Assertions.assertEquals(LocalDate.of(1992, 3, 7), contract.getBirth().getLocalDateBirthDate());
        Assertions.assertEquals(
                LocalDateProto.LocalDate.newBuilder().setMonth(3).setDayOfMonth(7).setYear(1992).build(),
                contract.getBirth().getBirthDate());
        Assertions.assertFalse(contract.getDeath().hasJavaLocalDateDeath());
        Assertions.assertNull(contract.getDeath().getJavaLocalDateDeath());
    }

    @Test
    public void testManyMessagesWithJavaOuterClass() {
        ManyMessagesWithJavaOuterClass.TestEvent eventData =
                ManyMessagesWithJavaOuterClass.TestEvent.newBuilder().build();
        Assertions.assertEquals(
                eventData.getEventTime(),
                Timestamp.getDefaultInstance());
        Assertions.assertNotNull(eventData.getInstantEventTime());
        Assertions.assertFalse(eventData.hasInstantEventTime());
        Assertions.assertFalse(eventData.hasEventTime());
        Instant defaultInstant = timestampToInstant.apply(Timestamp.getDefaultInstance());
        Assertions.assertEquals(eventData.getInstantEventTime(), defaultInstant);

        Instant now = Instant.now();
        Timestamp tsNow =
                Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();
        eventData =
                ManyMessagesWithJavaOuterClass.TestEvent.newBuilder()
                        .setEventTime(tsNow)
                        .build();
        Assertions.assertNotNull(eventData.getInstantEventTime());
        Assertions.assertTrue(eventData.hasEventTime());
        Assertions.assertTrue(eventData.hasInstantEventTime());

        Assertions.assertEquals(now, eventData.getInstantEventTime());
    }

    @Test
    public void testMultipleFilesModeWithProtoPackage() {
        Instant now = Instant.now();
        Timestamp tsNow =
                Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();
        TestSnapCreation sod = TestSnapCreation.newBuilder().setCreatedAt(tsNow).build();
        Assertions.assertTrue(sod.hasInstantCreatedAt());
        Assertions.assertEquals(now, sod.getInstantCreatedAt());

        TestSnapExpiration eod = TestSnapExpiration.newBuilder().setExpiredAt(tsNow).build();
        Assertions.assertTrue(eod.hasInstantExpiredAt());
        Assertions.assertEquals(now, eod.getInstantExpiredAt());

        eod = TestSnapExpiration.newBuilder().build();
        Assertions.assertFalse(eod.hasInstantExpiredAt());
        Assertions.assertEquals(
                Instant.ofEpochSecond(
                        Timestamp.getDefaultInstance().getSeconds(),
                        Timestamp.getDefaultInstance().getSeconds()),
                eod.getInstantExpiredAt());
    }

    @Test
    public void testMultipleFilesModeWithJavaPackage() {
        String discount = "7.392";
        TestEndOfSeasonSale eosSale =
                TestEndOfSeasonSale.newBuilder()
                        .setDiscount(StringValue.newBuilder().setValue(discount).build())
                        .build();
        Assertions.assertTrue(eosSale.hasBigDecimalDiscount());
        Assertions.assertEquals(
                0, eosSale.getBigDecimalDiscount().compareTo(new BigDecimal(discount)));
        Assertions.assertEquals(discount, eosSale.getDiscount().getValue());

        TestFlashSale flashSale = TestFlashSale.newBuilder().build();
        Assertions.assertNull(flashSale.getBdDiscount());
        Assertions.assertFalse(flashSale.hasBdDiscount());
    }

    @Test
    public void testMultipleFilesModeWithJavaOuterClass() {
        Instant now = Instant.now();
        Timestamp tsNow =
                Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();

        TestPayment testPayment = TestPayment.newBuilder().setPaidAt(tsNow).build();
        Assertions.assertEquals(now, testPayment.getInstantPaymentTime());
        Assertions.assertEquals(tsNow, testPayment.getPaidAt());
        Assertions.assertTrue(testPayment.hasInstantPaymentTime());

        TestAccount testAccount = TestAccount.newBuilder().build();
        Assertions.assertFalse(testAccount.hasAccountId());
    }

}
