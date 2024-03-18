package com.example.demo.dao;

import com.example.demo.resources.PropertiesRedis;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisConnection {
    RedisClient client;
    StatefulRedisConnection<String , String> connections;
    RedisCommands<String, String> syncCommand;

    PropertiesRedis redis;
    String key;
    public RedisConnection(){
        redis= new PropertiesRedis();
        key= redis.getKey();
        client= RedisClient.create(redis.getUrl());
        connections= client.connect();
        syncCommand= connections.sync();
    }

    public RedisCommands<String, String> getSyncCommand() {
        return syncCommand;
    }

    public String getKey() {
        return key;
    }

    void initialise(){
//        syncCommand.set(redis.getKey(), "Lettuce 1 program");



    }
}
