
# Create Controller
controller = """package com.urlvalidator.controller;

import com.urlvalidator.service.UrlChecker;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class UrlCheckController {

    @PostMapping("/check-url")
    public ResponseEntity<Map<String, String>> checkUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        
        if (url == null || url.trim().isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "❌ Please provide a valid URL");
            return ResponseEntity.badRequest().body(response);
        }
        
        System.out.println("Checking URL: " + url);
        
        boolean isValid = UrlChecker.isUrlReachable(url);
        String message;

        if (isValid) {
            message = "✅ Valid website URL";
            System.out.println("Result: VALID");
        } else {
            message = "❌ Invalid or unreachable website URL";
            System.out.println("Result: INVALID");
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "URL Validator API is running");
        return ResponseEntity.ok(response);
    }
}
"""

with open(f"{base_path}/src/main/java/com/urlvalidator/controller/UrlCheckController.java", "w") as f:
    f.write(controller)

print("Controller created!")
