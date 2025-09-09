package com.company.turtle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootRestController {

    @GetMapping("/status")
    public String home() {
        return "Turtle is running";
    }
}

