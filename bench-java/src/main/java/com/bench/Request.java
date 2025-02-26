package com.bench;

public class Request {
    private String method;
    private String endpoint;
    private String data;
    private String header;
    private int connections;
    private long rate;

    // Getters and setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public String getHeader() { return header; }
    public void setHeader(String header) { this.header = header; }
    
    public int getConnections() { return connections; }
    public void setConnections(int connections) { this.connections = connections; }
    
    public long getRate() { return rate; }
    public void setRate(long rate) { this.rate = rate; }
}