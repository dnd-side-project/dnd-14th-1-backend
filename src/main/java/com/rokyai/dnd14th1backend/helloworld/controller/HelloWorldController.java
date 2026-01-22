package com.rokyai.dnd14th1backend.helloworld.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.rokyai.dnd14th1backend.helloworld.service.HelloWorldService;

@RestController
@RequiredArgsConstructor
@RequestMapping(version = "0.0.1", path = "/hello-world")
public class HelloWorldController {

    private final HelloWorldService helloWorldService;

    @GetMapping("/hello")
    public String helloWorld() {
        return helloWorldService.getHelloMessage();
    }
}
