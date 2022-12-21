package com.github.lakunma.worktracker.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("dayAgo", 1);
        model.addAttribute("weekAgo", 2);
        model.addAttribute("monthAgo", 3);
        model.addAttribute("threeMonthsAgo", 4);
        return "home";
    }

}