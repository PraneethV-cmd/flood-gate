package com.bench;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Stats {
    private String endpoint;
    private double responseSize;
    private long responseDurationNanos;
    private AtomicLong totalRequests;
    private AtomicInteger errors;

    public Stats(String endpoint) {
        this.endpoint = endpoint;
        this.responseSize = 0;
        this.responseDurationNanos = 0;
        this.totalRequests = new AtomicLong(0);
        this.errors = new AtomicInteger(0);
    }

    public synchronized void update(int responseSize, long durationNanos, boolean hasError) {
        totalRequests.incrementAndGet();
        
        if (hasError) {
            errors.incrementAndGet();
            return;
        }
        
        this.responseSize += responseSize;
        this.responseDurationNanos += durationNanos;
    }

    public void calculateAverages() {
        long successfulRequests = totalRequests.get() - errors.get();
        if (successfulRequests > 0) {
            this.responseSize = this.responseSize / successfulRequests;
            this.responseDurationNanos = this.responseDurationNanos / successfulRequests;
        }
    }

    public void print() {
        System.out.printf("Test completed for endpoint: %s\n", endpoint);
        System.out.printf("\tTotal requests completed: %d\n", totalRequests.get());
        System.out.printf("\tTotal errors: %d\n", errors.get());
        System.out.printf("\tAverage response size: %.6f bytes\n", responseSize);
        System.out.printf("\tAverage response time: %.3f ms\n", responseDurationNanos / 1_000_000.0);
    }

    public String getEndpoint() {
        return endpoint;
    }
}