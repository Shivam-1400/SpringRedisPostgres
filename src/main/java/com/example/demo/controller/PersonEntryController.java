package com.example.demo.controller;

import com.example.demo.dao.DBConnection;
import com.example.demo.dao.RedisConnection;
import com.example.demo.entity.PersonEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/person")
public class PersonEntryController {
    RedisConnection redisConnection;
    DBConnection dbConnection;
    RedisCommands<String, String> syncCommand;

    ObjectMapper objectMapper;

    @GetMapping
    public String doSomethnig() throws JsonProcessingException {
        initialise();
        return "Doing something";
    }


    @GetMapping("/show-all")
    public List<PersonEntry> getAll() throws JsonProcessingException {
        initialise();
        List<String> redisRead= syncCommand.lrange(redisConnection.getKey(), 0, -1);
        objectMapper= new ObjectMapper();
        List<PersonEntry> personEntryList= new ArrayList<>();
        for(String json: redisRead){
            PersonEntry p= objectMapper.readValue(json, PersonEntry.class);
            personEntryList.add(p);
            System.out.println(p);
        }
        return personEntryList;
    }

    @PostMapping("/add-new")
    public String createEntry(@RequestBody PersonEntry newPersonEntry) throws JsonProcessingException, SQLException {
        long redisResult= syncCommand.lpush(redisConnection.getKey(), objectMapper.writeValueAsString(newPersonEntry));

        PreparedStatement pst= dbConnection.getConnection().prepareStatement("insert into demo_person(id, name, email) values(?, ?, ?');");
        pst.setInt(1, newPersonEntry.getId());
        pst.setString(2, newPersonEntry.getName());
        pst.setString(2, newPersonEntry.getEmail());

        int result= pst.executeUpdate();

        if(result==1){
            initialise();
            return "Data inserted";
        }

        return "Error";
    }

    @GetMapping("/id/{myId}")
    public PersonEntry getPersonEntryById(@PathVariable int myId) throws JsonProcessingException {

        List<String> redisRead= syncCommand.lrange(redisConnection.getKey(), 0, -1);

        objectMapper= new ObjectMapper();
        for(String json: redisRead){
            PersonEntry p= objectMapper.readValue(json, PersonEntry.class);
            if(p.getId()== myId){
                System.out.println(p);
                return p;
            }
        }
        initialise();
        redisRead= syncCommand.lrange(redisConnection.getKey(), 0, -1);

        objectMapper= new ObjectMapper();
        for(String json: redisRead){
            PersonEntry p= objectMapper.readValue(json, PersonEntry.class);
            if(p.getId()== myId){
                System.out.println(p);
                return p;
            }
        }
        return null;
    }

    @DeleteMapping("/id/{myId}")
    public String  deletePersonEntryById(@PathVariable int myId) throws JsonProcessingException, SQLException {
        PersonEntry personEntry= getPersonEntryById(myId);
        if(personEntry== null){
            return  "Error while deleting";
        }
        PreparedStatement pst= dbConnection.getConnection().prepareStatement("DELETE FROM demo_person WHERE id= ?");
        pst.setInt(1, personEntry.getId());
        int result= pst.executeUpdate();
        initialise();
        if(result==1){
            return personEntry+" Deleted ";
        }
        return  "Error while deleting";
    }

    @PutMapping("/id/{id}")
    public String updatePersonEntry(@PathVariable int id, @RequestBody PersonEntry myEntry) throws JsonProcessingException, SQLException {
        PersonEntry personEntry= getPersonEntryById(id);
        if(personEntry== null){
            return  "Error while updating";
        }

        PreparedStatement pst= dbConnection.getConnection().prepareStatement("DELETE FROM demo_person WHERE id= ?");
        pst.setInt(1, personEntry.getId());
        pst.executeUpdate();

        createEntry(myEntry);
        int result= pst.executeUpdate();
        if(result==1){
            initialise();
            return personEntry+" updated ";
        }
        return  "Error while updating";
    }

    void initialise() throws JsonProcessingException {
        redisConnection= new RedisConnection();
        syncCommand= redisConnection.getSyncCommand();

        if(redisConnection== null || syncCommand== null){}
//        syncCommand.lpush(redisConnection.getKey(), objectMapper.writeValueAsString(new PersonEntry(12, "karan", "12@12.com")));

        List<String> redisRead= syncCommand.lrange(redisConnection.getKey(), 0, -1);

        if(redisRead== null) {
            System.out.println("Cache miss");
            dbConnection = new DBConnection();
            try {
                PreparedStatement pst = dbConnection.getConnection().prepareStatement("SELECT * FROM demo_person;");
                ResultSet resultSet = pst.executeQuery();
                while (resultSet.next()){
                    PersonEntry p= new PersonEntry(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
                    syncCommand.lpush(redisConnection.getKey(), objectMapper.writeValueAsString(p));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            objectMapper= new ObjectMapper();
            for(String json: redisRead){
                PersonEntry p= objectMapper.readValue(json, PersonEntry.class);
                System.out.println(p);
            }
        }
    }
}




//[
//        {
//        "id": 1,
//        "name": "name1",
//        "email": "name1@mail.com"
//        },
//        {
//        "id": 2,
//        "name": "name2",
//        "email": "name2@email.com"
//        }
//        ]