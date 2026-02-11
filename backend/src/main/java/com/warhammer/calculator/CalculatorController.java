package com.warhammer.calculator;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200") // FE: 4200, BE: 8080
public class CalculatorController {

    @GetMapping("/increment/{num}")
    public int increment(@PathVariable int num) {
        return num + 1;
    }
}