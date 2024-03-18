package com.example.demo.resources;

public class PropertiesPostgres {
    private String url;
    private String user;
    private String password;

    public PropertiesPostgres(){
        url= "jdbc:postgresql://127.0.0.1:5432/netcore1";
        user= "postgres";
        password= "mysecretpassword";

    }

    public PropertiesPostgres(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }


    public String getUser() {
        return user;
    }


    public String getPassword() {
        return password;
    }

}
