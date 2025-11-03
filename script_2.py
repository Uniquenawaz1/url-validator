
# Create Main Application class
main_app = """package com.urlvalidator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UrlValidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlValidatorApplication.class, args);
        System.out.println("\\n========================================");
        System.out.println("URL Validator Application Started!");
        System.out.println("Access the app at: http://localhost:8080");
        System.out.println("API Endpoint: http://localhost:8080/api/check-url");
        System.out.println("========================================\\n");
    }
}
"""

with open(f"{base_path}/src/main/java/com/urlvalidator/UrlValidatorApplication.java", "w") as f:
    f.write(main_app)

print("Main Application class created!")
