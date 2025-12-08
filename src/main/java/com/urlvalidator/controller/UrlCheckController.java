package com.urlvalidator.controller;

import com.urlvalidator.service.UrlChecker;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UrlCheckController {

    private final UrlChecker checker;

    public UrlCheckController(UrlChecker checker) {
        this.checker = checker;
    }

    @PostMapping("/check-url")
    public ResponseEntity<?> checkUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Missing url field"));
        }
        boolean ok = checker.isReachable(url);
        if (ok) {
            return ResponseEntity.ok(Map.of("message", "✅ Valid website URL"));
        }
        return ResponseEntity.ok(Map.of("message", "❌ Invalid or unreachable website URL"));
    }

    // Debug endpoint: accept any content-type and return the raw body so we can see what the server receives.
    @PostMapping(path = "/check-url-raw", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<?> checkUrlRaw(@RequestBody(required = false) String rawBody) {
        if (rawBody == null) rawBody = "";
        return ResponseEntity.ok(Map.of(
                "receivedBody", rawBody
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
