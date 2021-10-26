package net.lamgc.oracle.sentry.common.retry;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryerTest {

    @Test
    void successTest() throws RetryFailedException, ExecutionException, InterruptedException {
        final Object obj = new Object();
        Retryer<Object> retryer = Retryer.builder(() -> obj).create();
        assertEquals(obj, retryer.execute());
        assertEquals(obj, retryer.executeAsync().get());
    }

    @Test
    void failedTest() {
        assertThrows(RetryFailedException.class, () -> {
            Retryer<Object> retryer = Retryer.builder(() -> {
                throw new RuntimeException();
            }).create();
            retryer.execute();
        });
    }

    @Test
    void retryNumberTest() {
        final int retryNumber = new Random().nextInt(9) + 1;
        final AtomicInteger retryCounter = new AtomicInteger(-1);
        Retryer<Object> retryer = Retryer.builder(() -> {
            retryCounter.incrementAndGet();
            throw new RuntimeException();
        }).retryNumber(retryNumber).create();
        try {
            retryer.execute();
        } catch (RetryFailedException e) {
            e.printStackTrace();
        }
        assertEquals(retryNumber, retryCounter.get());
    }

    @Test
    void checkerTest() {
        Retryer<Object> retryer = Retryer.builder(() -> null)
                .retryIfReturnNull()
                .create();
        assertThrows(RetryFailedException.class, retryer::execute);
    }


}