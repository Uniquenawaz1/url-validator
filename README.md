# URL Validator - Spring Boot Application

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
curl -X POST http://localhost:8080/api/check-url \
  -H "Content-Type: application/json" \
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

## Hosting on GitHub (public)

1. Create a new GitHub repository (public) and push this project.

2. Recommended: Enable GitHub Actions and create the following secrets for publishing to GitHub Container Registry (GHCR):
  - `GHCR_TOKEN` (Personal access token with `read:packages` and `write:packages`) or use `GITHUB_TOKEN` for same-repo pushes.

3. After pushing, Actions will build the Docker image and upload a release JAR (workflows are included in `.github/workflows`).

4. Once the image is published you can run:

```powershell
docker pull ghcr.io/<your-org-or-username>/url-validator:latest
docker run -d -p 8080:8080 ghcr.io/<your-org-or-username>/url-validator:latest
```

Notes: If you want me to configure the workflows to your organization or create a draft release after push, tell me the repository name and whether to use GHCR or Docker Hub.

Contributing / Pushing

1. Create a new repository on GitHub (public/private) named `url-validator` (or your preferred name).
2. On your machine:

```powershell
cd d:\NZPA\url_validator
git init
git add .
git commit -m "Initial import"
git remote add origin https://github.com/<your-username>/url-validator.git
git branch -M main
git push -u origin main
```

3. Actions will run on `main` pushes. To publish the release JAR, create a tag and push it:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

Testing the running API

1. Simple curl example:

```bash
curl -X GET http://localhost:8080/api/health

curl -X POST http://localhost:8080/api/check-url -H "Content-Type: application/json" -d '{"url":"https://google.com"}'
```

2. PowerShell quick test (included):

```powershell
cd d:\NZPA\url_validator
.\test-api.ps1
```

3. Integrations: call `/api/check-url` from any app (Node/Python/Java) using normal HTTP client libraries.


