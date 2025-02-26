package com.bench;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Config {
    private List<Request> requests;
    private String host;
    private long duration;

    // Getters and setters
    public List<Request> getRequests() { return requests; }
    public void setRequests(List<Request> requests) { this.requests = requests; }
    
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    
    // Load config from JSON file
    public static Config fromJSON(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(path), Config.class);
    }
}