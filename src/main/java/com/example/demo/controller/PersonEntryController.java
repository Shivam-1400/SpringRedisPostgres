package com.example.demo.controller;

import com.example.demo.dao.DBConnection;
import com.example.demo.dao.RedisConnection;
import com.example.demo.entity.PersonEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/person")
public class PersonEntryController {
    Logger logger= LoggerFactory.getLogger(PersonEntryController.class);
    RedisConnection redisConnection;
    DBConnection dbConnection;
    RedisCommands<String, String> syncCommand;

    ObjectMapper objectMapper;

    @GetMapping
    public String doSomething() throws JsonProcessingException {
        initialise();
        logger.info("Shivam: Doing Something. Initialising");
        return "Doing something";
    }


    @GetMapping("/show-all")
    public List<PersonEntry> getAll() throws JsonProcessingException {
        List<String> redisRead= syncCommand.lrange(redisConnection.getKey(), 0, -1);
        if( !redisRead.isEmpty()){
            objectMapper= new ObjectMapper();
            List<PersonEntry> personEntryList= new ArrayList<>();
            for(String json: redisRead){
                PersonEntry p= objectMapper.readValue(json, PersonEntry.class);
                personEntryList.add(p);
                System.out.println(p);
            }
            return personEntryList;
        }
        initialise();
        redisRead= syncCommand.lrange(redisConnection.getKey(), 0, -1);
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
    public void createEntry(@RequestBody PersonEntry newPersonEntry) throws JsonProcessingException, SQLException {
        long redisResult= syncCommand.lpush(redisConnection.getKey(), objectMapper.writeValueAsString(newPersonEntry));

        PreparedStatement pst= dbConnection.getConnection().prepareStatement("insert into demo_person(id, name, email) values(?, ?, ?);");
        pst.setString(1, newPersonEntry.getId());
        pst.setString(2, newPersonEntry.getName());
        pst.setString(3, newPersonEntry.getEmail());

        int result= pst.executeUpdate();

        if(result==1){
            initialise();
            logger.info("Shivam: Data inserted");
        }
        else{
            logger.error("Shivam: Error. Person Not inserted");
        }
    }

    @GetMapping("/id/{myId}")
    public PersonEntry getPersonEntryById(@PathVariable String myId) throws JsonProcessingException {

        List<String> redisRead= syncCommand.lrange(redisConnection.getKey(), 0, -1);

        objectMapper= new ObjectMapper();
        for(String json: redisRead){
            PersonEntry p= objectMapper.readValue(json, PersonEntry.class);
            if(Objects.equals(p.getId(), myId)){
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
    public void deletePersonEntryById(@PathVariable String myId) throws JsonProcessingException, SQLException {
        PersonEntry personEntry= getPersonEntryById(myId);
        if(personEntry== null){
            logger.error("Shivam: Error while deleting since ID not found");
            return;
        }
        PreparedStatement pst= dbConnection.getConnection().prepareStatement("DELETE FROM demo_person WHERE id= ?");
        pst.setString(1, personEntry.getId());
        int result= pst.executeUpdate();
        initialise();
        if(result==1){
            logger.info("Shivam: "+personEntry+" Deleted.");
            return;
        }
        logger.error("Shivam: Error while deleting");
    }

    @PutMapping("/id/{id}")
    public void updatePersonEntry(@PathVariable String id, @RequestBody PersonEntry myEntry) throws JsonProcessingException, SQLException {
        PersonEntry personEntry= getPersonEntryById(id);
        if(personEntry== null){
            logger.error("Shivam: Error while updating");
            return;
        }

        PreparedStatement pst= dbConnection.getConnection().prepareStatement("DELETE FROM demo_person WHERE id= ?");
        pst.setString(1, personEntry.getId());
        pst.executeUpdate();

        createEntry(myEntry);
//        int result= pst.executeUpdate();
//        if(result==1){
//            initialise();
//            logger.info("Shivam: " +personEntry+" updated ");
//        }
//        else{
//            logger.error("Shivam: Error while updating");
//        }

    }

    void initialise() throws JsonProcessingException {
        redisConnection= new RedisConnection();
        syncCommand= redisConnection.getSyncCommand();
        dbConnection= new DBConnection();

        if(redisConnection== null || syncCommand== null){
            logger.info("Shivam: Error in initialisation with redis");
        }
        else{
            logger.info("Shivam: Redis connected");
            syncCommand.flushall();
        }
        objectMapper= new ObjectMapper();
        List<String> redisRead= syncCommand.lrange(redisConnection.getKey(), 0, -1);

        if(redisRead.isEmpty()) {
            dbConnection = new DBConnection();
            if(dbConnection.getConnection()== null ){
                logger.info("Shivam: Error in initialisation with postgres");
            }
            else{
                logger.info("Shivam: Postgres connected");
            }
            try {
                PreparedStatement pst = dbConnection.getConnection().prepareStatement("SELECT * FROM demo_person;");
                ResultSet resultSet = pst.executeQuery();
                while (resultSet.next()){
                    PersonEntry p= new PersonEntry(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3));
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