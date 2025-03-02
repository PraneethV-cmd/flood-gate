package com.bench;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Bench {
    private final List<LoadTester> testers;
    private final BlockingQueue<Stats> statsQueue;
    private final CountDownLatch latch;

    public static Bench create(String configPath) throws IOException {
        Config config = Config.fromJSON(configPath);
        List<LoadTester> testers = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(config.getRequests().size());
        BlockingQueue<Stats> statsQueue = new LinkedBlockingQueue<>();
        
        for (Request request : config.getRequests()) {
            LoadTester tester = new LoadTester(
                config.getHost(), 
                request, 
                config.getDuration(), 
                statsQueue,
                latch
            );
            testers.add(tester);
        }
        
        return new Bench(testers, statsQueue, latch);
    }

    private Bench(List<LoadTester> testers, BlockingQueue<Stats> statsQueue, CountDownLatch latch) {
        this.testers = testers;
        this.statsQueue = statsQueue;
        this.latch = latch;
    }

    public void run() {
        // Start all testers in their own threads
        for (LoadTester tester : testers) {
            new Thread(tester).start();
        }

        try {
            // Wait for all testers to complete
            latch.await();
            
            // Process and display all stats
            int expectedResults = testers.size();
            for (int i = 0; i < expectedResults; i++) {
                Stats stats = statsQueue.poll(1, TimeUnit.SECONDS);
                if (stats != null) {
                    stats.print();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Benchmark was interrupted: " + e.getMessage());
        }
    }
}