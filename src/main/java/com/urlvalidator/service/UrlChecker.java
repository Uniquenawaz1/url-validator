package com.urlvalidator.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class UrlChecker {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    public UrlChecker() {
        System.out.println("[UrlChecker] Initialized");
    }

    public boolean isReachable(String url) {
        System.out.println("[UrlChecker.isReachable] Called with: " + url);
        
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            System.out.println("[UrlChecker] URL to check: " + url);
            
            // Parse URL to validate format
            URI uri = new URI(url);
            System.out.println("[UrlChecker] URI parsed successfully");
            
            // Try with async HTTP client to avoid blocking issues
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            
            System.out.println("[UrlChecker] HttpClient created");
            
            // Create HEAD request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(10))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .header("User-Agent", USER_AGENT)
                    .build();
            
            System.out.println("[UrlChecker] Request created, attempting to send...");
            
            // Use sendAsync with a timeout wrapper
            CompletableFuture<HttpResponse<Void>> future = httpClient.sendAsync(
                    request, 
                    HttpResponse.BodyHandlers.discarding()
            );
            
            System.out.println("[UrlChecker] Async request sent");
            
            HttpResponse<Void> response = future.get(15, TimeUnit.SECONDS);
            
            System.out.println("[UrlChecker] Got response: " + response.statusCode());
            
            if (response.statusCode() >= 200 && response.statusCode() < 400) {
                System.out.println("[UrlChecker] ✓ URL is reachable");
                return true;
            }
            
            // If HEAD returned error, try GET
            System.out.println("[UrlChecker] HEAD failed with status " + response.statusCode() + ", trying GET");
            
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .header("User-Agent", USER_AGENT)
                    .build();
            
            CompletableFuture<HttpResponse<Void>> getfuture = httpClient.sendAsync(
                    getRequest,
                    HttpResponse.BodyHandlers.discarding()
            );
            
            HttpResponse<Void> getResponse = getfuture.get(20, TimeUnit.SECONDS);
            
            System.out.println("[UrlChecker] GET response: " + getResponse.statusCode());
            
            return getResponse.statusCode() >= 200 && getResponse.statusCode() < 400;
            
        } catch (java.util.concurrent.TimeoutException e) {
            System.out.println("[UrlChecker] Timeout: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("[UrlChecker] Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}



