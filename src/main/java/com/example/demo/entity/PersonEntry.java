package com.example.demo.entity;

public class PersonEntry {
    private String id;
    private String name;

    public PersonEntry() {
    }

    public PersonEntry(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    private String email;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "PersonEntry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
