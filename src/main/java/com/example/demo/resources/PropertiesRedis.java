package com.example.demo.resources;

public class PropertiesRedis {

    private String url;
    private String key;

    public PropertiesRedis(){
        this.url= "redis://password@localhost:6379/0";
        this.key= "demoSpring1";
    }

    public PropertiesRedis(String url, String key) {
        this.url = url;
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public String getKey() {
        return key;
    }




}
