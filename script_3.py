
# Create UrlChecker service
url_checker = """package com.urlvalidator.service;

import java.net.HttpURLConnection;
import java.net.URL;

public class UrlChecker {

    public static boolean isUrlReachable(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // Set browser-like headers to avoid bot detection
            connection.setRequestMethod("HEAD");
            connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Accept", 
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            
            // If HEAD method is not allowed (405) or forbidden (403), try GET
            if (responseCode == 405 || responseCode == 403) {
                connection.disconnect();
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", 
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
                connection.setRequestProperty("Accept", "text/html,application/xhtml+xml");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setInstanceFollowRedirects(true);
                responseCode = connection.getResponseCode();
            }

            connection.disconnect();
            
            // Accept any HTTP response (2xx, 3xx, 4xx, 5xx) as valid
            // Only network failures (exceptions) indicate invalid URL
            return (responseCode >= 200 && responseCode < 600);
            
        } catch (java.net.UnknownHostException e) {
            // Domain doesn't exist
            System.out.println("Unknown host: " + urlString);
            return false;
        } catch (java.net.SocketTimeoutException e) {
            // Connection timeout - site might be slow but exists
            // Being lenient here - you can change to false if needed
            System.out.println("Timeout for: " + urlString);
            return false;
        } catch (java.net.MalformedURLException e) {
            // Invalid URL format
            System.out.println("Malformed URL: " + urlString);
            return false;
        } catch (Exception e) {
            // Other connection errors
            System.out.println("Error checking URL " + urlString + ": " + e.getMessage());
            return false;
        }
    }
}
"""

with open(f"{base_path}/src/main/java/com/urlvalidator/service/UrlChecker.java", "w") as f:
    f.write(url_checker)

print("UrlChecker service created!")
