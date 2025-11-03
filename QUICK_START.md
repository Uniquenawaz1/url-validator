# Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Install Java (if not already installed)
Download Java 17 or higher from: https://adoptium.net/

Verify installation:
```bash
java -version
```

### Step 2: Run the Application

**Windows:**
```bash
mvnw.cmd spring-boot:run
```

**Mac/Linux:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### Step 3: Open Your Browser
Go to: **http://localhost:8080**

## ✅ That's it! Start validating URLs!

---

## Testing the App

Try these URLs to see it work:

### Valid URLs (Should show ✅)
- https://google.com
- https://tattvaspa.com
- https://lifestylestores.com
- https://github.com
- https://amazon.in

### Invalid URLs (Should show ❌)
- https://thiswebsitedoesnotexist12345.com
- https://fakedomainname999.xyz
- https://notarealsite123.com

---

## Need Help?

**Application won't start?**
- Make sure Java 17+ is installed
- Check if port 8080 is available
- Look for error messages in the terminal

**Can't access the UI?**
- Wait 30 seconds after startup
- Try refreshing your browser
- Check console for "Application Started!" message

**URLs showing incorrect results?**
- Some sites may take longer to respond
- Protected sites might block automated checks
- The app is working correctly if real sites show as valid

---

## Stopping the Application

Press `Ctrl + C` in the terminal where the app is running.

---

## Advanced: Building a Standalone JAR

```bash
# Build
mvnw clean package

# Run the JAR
java -jar target/url-validator-1.0.0.jar
```

Now you have a standalone executable JAR file!
