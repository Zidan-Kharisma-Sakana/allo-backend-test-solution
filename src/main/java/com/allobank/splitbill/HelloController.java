package com.allobank.splitbill;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Operation(summary = "..", description = ".")
    @GetMapping("/hello")
    public Greeting hello() {
        return new Greeting("wassup");
    }

    public record Greeting(String message) {}
}
