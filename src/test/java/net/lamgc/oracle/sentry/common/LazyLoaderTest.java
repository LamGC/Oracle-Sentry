package net.lamgc.oracle.sentry.common;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class LazyLoaderTest {

    @Test
    @SuppressWarnings("rawtypes")
    public void lazyLoadTest() throws NoSuchFieldException, IllegalAccessException {
        LazyLoader<Object> loader = new LazyLoader<>(Object::new);

        Field field = LazyLoader.class.getDeclaredField("object");
        field.setAccessible(true);
        AtomicReference reference = (AtomicReference) field.get(loader);
        assertNotNull(reference);
        assertNull(reference.get());
        Object instance = loader.getInstance();
        assertEquals(reference.get(), instance);
        assertEquals(reference.get(), loader.getInstance());
        assertEquals(instance, loader.getInstance());
    }

    @Test
    @SuppressWarnings("StatementWithEmptyBody")
    public void multiThreadAccessTest() {
        class Singleton {
            private final static AtomicInteger constructNum = new AtomicInteger(0);
            public Singleton() {
                if (constructNum.incrementAndGet() > 1) {
                    fail("Multiple instances were generated.");
                }
            }
        }

        final LazyLoader<Singleton> loader = new LazyLoader<>(Singleton::new);
        AtomicBoolean start = new AtomicBoolean(false);
        int threadNum = Runtime.getRuntime().availableProcessors();
        List<Thread> threads = new ArrayList<>(threadNum);
        for (int i = 0; i < threadNum; i++) {
            Thread thread = new Thread(() -> {
                while (!start.get()) {
                }
                assertNotNull(loader.getInstance());
            });
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        start.set(true);
    }
    

}