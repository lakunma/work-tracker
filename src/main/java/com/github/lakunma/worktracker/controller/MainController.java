package com.github.lakunma.worktracker.controller;

import com.github.lakunma.worktracker.workingdates.DayType;
import com.github.lakunma.worktracker.workingdates.DayTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    private final DayTypeRepository dayTypeRepository;

    @Autowired
    public MainController(DayTypeRepository dayTypeRepository) {
        this.dayTypeRepository = dayTypeRepository;
    }

    @GetMapping("/daytypes")
    public String daytypes(Model model) {
        Iterable<DayType> dayTypes = dayTypeRepository.findAll();
        model.addAttribute("dayTypes", dayTypes);
        return "daytypes";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("dayAgo", 1);
        model.addAttribute("weekAgo", 2);
        model.addAttribute("monthAgo", 3);
        model.addAttribute("threeMonthsAgo", 4);
        return "home";
    }

}