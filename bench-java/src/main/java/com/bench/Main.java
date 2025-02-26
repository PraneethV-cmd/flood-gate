package com.bench;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar bench-java.jar config.json");
            System.exit(1);
        }

        String configPath = args[0];
        
        try {
            Bench bench = Bench.create(configPath);
            bench.run();
        } catch (IOException e) {
            System.err.println("Could not open file: " + e.getMessage());
            System.exit(1);
        }
    }
}