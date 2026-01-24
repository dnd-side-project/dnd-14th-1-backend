package com.rokyai.dnd14th1backend.helloworld.service;

import org.springframework.stereotype.Service;

@Service
public class HelloWorldService {

    public String getHelloMessage() {
        return "Hello, World!";
    }
}
