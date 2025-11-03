
# Create README.md
readme = """# URL Validator - Spring Boot Application

A professional URL validation tool that checks if websites are valid and reachable.

## Features
- ✅ Accurate URL validation using server-side checking
- ✅ Handles bot-protected sites (Cloudflare, etc.)
- ✅ Clean, modern UI
- ✅ REST API endpoint
- ✅ No browser CORS limitations

## Technology Stack
- **Backend**: Spring Boot 3.2.0 (Java 17)
- **Frontend**: HTML, CSS, JavaScript
- **Build Tool**: Maven

## Prerequisites
- Java 17 or higher
- Maven 3.6+ (or use included Maven wrapper)

## How to Run

### Option 1: Using Maven Wrapper (Recommended)
```bash
# On Windows
mvnw.cmd spring-boot:run

# On Mac/Linux
./mvnw spring-boot:run
```

### Option 2: Using Installed Maven
```bash
mvn spring-boot:run
```

### Option 3: Build JAR and Run
```bash
# Build
mvn clean package

# Run
java -jar target/url-validator-1.0.0.jar
```

## Access the Application

Once started, the application will be available at:
- **Frontend UI**: http://localhost:8080
- **API Endpoint**: http://localhost:8080/api/check-url
- **Health Check**: http://localhost:8080/api/health

## API Usage

### Check URL Endpoint

**POST** `/api/check-url`

**Request Body:**
```json
{
  "url": "https://example.com"
}
```

**Response:**
```json
{
  "message": "✅ Valid website URL"
}
```

or

```json
{
  "message": "❌ Invalid or unreachable website URL"
}
```

### Example cURL Command
```bash
curl -X POST http://localhost:8080/api/check-url \\
  -H "Content-Type: application/json" \\
  -d '{"url": "https://google.com"}'
```

## Testing

Try these URLs:
- ✅ Valid: https://google.com
- ✅ Valid: https://tattvaspa.com
- ✅ Valid: https://lifestylestores.com
- ❌ Invalid: https://thiswebsitedoesnotexist12345.com

## How It Works

1. **Format Validation**: Checks if URL is properly formatted
2. **Server-Side Request**: Makes HTTP request from server (bypasses browser CORS)
3. **Smart Detection**: 
   - HTTP 200-399: Valid
   - HTTP 400-599: Valid (site exists, may have restrictions)
   - Connection failure/DNS error: Invalid

## Project Structure
```
url-validator-spring-boot/
├── src/
│   └── main/
│       ├── java/com/urlvalidator/
│       │   ├── UrlValidatorApplication.java
│       │   ├── controller/
│       │   │   └── UrlCheckController.java
│       │   └── service/
│       │       └── UrlChecker.java
│       └── resources/
│           ├── application.properties
│           └── static/
│               └── index.html
├── pom.xml
└── README.md
```

## Configuration

Edit `src/main/resources/application.properties` to change:
- Server port (default: 8080)
- Logging levels
- Other Spring Boot settings

## Troubleshooting

**Port 8080 already in use?**
```bash
# Change port in application.properties
server.port=8081
```

**Can't connect from frontend?**
- Ensure Spring Boot is running
- Check console for "URL Validator Application Started!" message
- Verify CORS is enabled in controller (@CrossOrigin annotation)

## License
MIT License - Free to use and modify

## Author
Built for reliable URL validation without browser limitations
"""

with open(f"{base_path}/README.md", "w") as f:
    f.write(readme)

print("README.md created!")
