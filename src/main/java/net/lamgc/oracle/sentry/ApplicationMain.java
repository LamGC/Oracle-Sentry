package net.lamgc.oracle.sentry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

/**
 * <p> 程序入口
 * @author LamGC
 */
@SpringBootApplication
@Configuration
public class ApplicationMain {

    private final static Logger log = LoggerFactory.getLogger(ApplicationMain.class);

    @SuppressWarnings("AlibabaConstantFieldShouldBeUpperCase")
    private final static Object mainThreadWaiter = new Object();

    /**
     * 程序入口.
     * @param args 程序参数.
     */
    public static void main(String[] args) {
        SpringApplication.run(ApplicationMain.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (mainThreadWaiter) {
                mainThreadWaiter.notifyAll();
            }
        }, "ShutdownMainThread"));

        synchronized (mainThreadWaiter) {
            try {
                mainThreadWaiter.wait();
            } catch (InterruptedException e) {
                log.warn("主线程发生中断.", e);
            }
        }
    }

}
