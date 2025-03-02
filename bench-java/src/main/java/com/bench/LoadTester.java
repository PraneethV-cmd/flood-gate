package com.bench;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class LoadTester implements Runnable {
    private final String endpoint;
    private final String url;
    private final String method;
    private final String data;
    private final int connections;
    private final long durationMillis;
    private final long rateMillis;
    private final Stats stats;
    private final BlockingQueue<Stats> statsQueue;
    private final CountDownLatch latch;
    private final HttpClient httpClient;

    public LoadTester(String host, Request request, long durationSeconds, BlockingQueue<Stats> statsQueue, CountDownLatch latch) {
        this.endpoint = request.getEndpoint();
        this.url = host + request.getEndpoint();
        this.method = request.getMethod();
        this.data = request.getData();
        this.connections = request.getConnections();
        this.durationMillis = durationSeconds * 1000;
        this.rateMillis = request.getRate();
        this.stats = new Stats(endpoint);
        this.statsQueue = statsQueue;
        this.latch = latch;
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public void run() {
        try {
            CountDownLatch connectionsLatch = new CountDownLatch(connections);
            System.out.printf("Running test on %s with %d connections making a request every %d ms\n", 
                          endpoint, connections, rateMillis);

            for (int i = 0; i < connections; i++) {
                new Thread(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        while (System.currentTimeMillis() - startTime < durationMillis) {
                            test();
                            Thread.sleep(rateMillis);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        connectionsLatch.countDown();
                    }
                }).start();
            }

            connectionsLatch.await();
            stats.calculateAverages();
            statsQueue.put(stats);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            latch.countDown();
        }
    }

    private void test() {
        long startTime = System.nanoTime();
        boolean hasError = false;
        int responseSize = 0;
    
        try {
            HttpUriRequest request;
            if (method.equalsIgnoreCase("GET")) {
                request = new HttpGet(url);
            } else if (method.equalsIgnoreCase("POST")) {
                HttpPost postRequest = new HttpPost(url);
                if (data != null && !data.isEmpty()) {
                    postRequest.setEntity(new StringEntity(data));
                }
                request = postRequest;
            } else {
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }
    
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            
            // Consider any non-2xx status code as an error
            if (statusCode < 200 || statusCode >= 300) {
                hasError = true;
                System.out.println("Received error status code: " + statusCode + " for " + url);
            } else {
                String responseBody = EntityUtils.toString(response.getEntity());
                responseSize = responseBody.length();
            }
        } catch (IOException e) {
            hasError = true;
            System.out.println("Error connecting to " + url + ": " + e.getMessage());
        }
    
        long durationNanos = System.nanoTime() - startTime;
        stats.update(responseSize, durationNanos, hasError);
    }
}