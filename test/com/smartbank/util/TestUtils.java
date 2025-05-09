package com.smartbank.util;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Assertions;

/**
 * Utility methods for testing SmartBank application components.
 */
public class TestUtils {
    
    /**
     * Default timeout for waiting operations in milliseconds.
     */
    public static final long DEFAULT_TIMEOUT_MS = 5000;
    
    /**
     * Default polling interval for waiting operations in milliseconds.
     */
    public static final long DEFAULT_POLL_INTERVAL_MS = 100;
    
    /**
     * Wait for a condition to be true with a timeout.
     * 
     * @param condition The condition to wait for
     * @param timeoutMs Maximum time to wait in milliseconds
     * @param message Message for timeout exception
     * @throws InterruptedException If the thread is interrupted
     * @throws AssertionError If the timeout is reached
     */
    public static void waitFor(BooleanSupplier condition, long timeoutMs, String message) 
            throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                Assertions.fail(message + " (timeout: " + timeoutMs + "ms)");
            }
            Thread.sleep(DEFAULT_POLL_INTERVAL_MS);
        }
    }
    
    /**
     * Wait for a condition to be true with the default timeout.
     * 
     * @param condition The condition to wait for
     * @param message Message for timeout exception
     * @throws InterruptedException If the thread is interrupted
     * @throws AssertionError If the timeout is reached
     */
    public static void waitFor(BooleanSupplier condition, String message) 
            throws InterruptedException {
        waitFor(condition, DEFAULT_TIMEOUT_MS, message);
    }
    
    /**
     * Execute an operation with a timeout.
     * 
     * @param <T> The return type of the operation
     * @param operation The operation to execute
     * @param timeoutMs The timeout in milliseconds
     * @param message Message for timeout exception
     * @return The result of the operation
     * @throws Exception If the operation throws an exception
     */
    public static <T> T executeWithTimeout(Callable<T> operation, long timeoutMs, String message) 
            throws Exception {
        long startTime = System.currentTimeMillis();
        Exception lastException = null;
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                Thread.sleep(DEFAULT_POLL_INTERVAL_MS);
            }
        }
        
        if (lastException != null) {
            throw lastException;
        }
        
        throw new AssertionError(message + " (timeout: " + timeoutMs + "ms)");
    }
    
    /**
     * Execute an operation with the default timeout.
     * 
     * @param <T> The return type of the operation
     * @param operation The operation to execute
     * @param message Message for timeout exception
     * @return The result of the operation
     * @throws Exception If the operation throws an exception
     */
    public static <T> T executeWithTimeout(Callable<T> operation, String message) 
            throws Exception {
        return executeWithTimeout(operation, DEFAULT_TIMEOUT_MS, message);
    }
}
