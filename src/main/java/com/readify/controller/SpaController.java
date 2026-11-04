package com.readify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/login", "/catalog", "/books/{id}", "/my-loans", "/admin"})
    public String spa() {
        return "forward:/index.html";
    }
}
