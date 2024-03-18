package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectionCheck {
    @GetMapping ("/connection-check")
    public String heathCheck(){
        return "OK. All set to go.";
    }
}
