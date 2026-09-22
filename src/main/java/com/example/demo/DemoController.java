package com.example.demo;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
public class DemoController {

    private final AtomicLong counter = new AtomicLong();

    /** Welcome message at the root. */
    @GetMapping("/hello")
    public Map<String, Object> hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Hello, " + name + "!");
        result.put("id", counter.incrementAndGet());
        result.put("serverTime", Instant.now().toString());
        return result;
    }

    /** Health / info endpoint. */
    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("app", "springboot-api-sample");
        result.put("version", "1.0.0");
        result.put("status", "UP");
        result.put("javaVersion", System.getProperty("java.version"));
        result.put("uptime", System.currentTimeMillis());
        return result;
    }

    /** Echo endpoint — echoes back what you send. */
    @PostMapping("/echo")
    public Map<String, Object> echo(@RequestBody(required = false) String body) {
        Map<String, Object> result = new HashMap<>();
        result.put("method", "POST");
        result.put("body", body == null || body.isEmpty() ? "(empty body)" : body);
        result.put("receivedAt", Instant.now().toString());
        return result;
    }

    /** Simple calculator: sum of a + b. */
    @GetMapping("/add")
    public Map<String, Object> add(@RequestParam double a, @RequestParam double b) {
        Map<String, Object> result = new HashMap<>();
        result.put("a", a);
        result.put("b", b);
        result.put("sum", a + b);
        result.put("timestamp", Instant.now().toString());
        return result;
    }

    /** Greeting. */
    @GetMapping("/greet/{name}")
    public Map<String, Object> greet(@PathVariable String name) {
        Map<String, Object> result = new HashMap<>();
        result.put("greeting", "Welcome back, " + name + "!");
        return result;
    }

    /** Current time endpoint. */
    @GetMapping("/time")
    public Map<String, Object> time() {
        Map<String, Object> result = new HashMap<>();
        result.put("serverTime", Instant.now().toString());
        result.put("epochMillis", System.currentTimeMillis());
        result.put("localTime", java.time.LocalDateTime.now().toString());
        result.put("zone", java.time.ZoneId.systemDefault().toString());
        return result;
    }
}
