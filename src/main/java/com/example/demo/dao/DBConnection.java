package com.example.demo.dao;

import com.example.demo.resources.PropertiesPostgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private Connection con;
    public DBConnection(){
        PropertiesPostgres properties= new PropertiesPostgres();
        try {
            con= DriverManager.getConnection(properties.getUrl(), properties.getUser(), properties.getPassword());

            if(con== null){
                System.out.println("Failed to connect to DB. Retry.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public Connection getConnection(){
        return con;
    }
}
