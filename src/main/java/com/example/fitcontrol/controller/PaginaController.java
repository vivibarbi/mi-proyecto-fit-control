package com.example.fitcontrol.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginaController {

    @GetMapping("/")
    public String inicio() {
        return "forward:/index.html";
    }

    /*@GetMapping("/calculadora")
    public String calculadora() {
        return "calculadora";
    }*/
}