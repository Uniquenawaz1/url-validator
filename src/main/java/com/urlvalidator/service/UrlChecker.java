package com.urlvalidator.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class UrlChecker {

    private final HttpClient client;

    public UrlChecker() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public boolean isReachable(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            
            final String finalUrl = url;
            final boolean[] result = {false};
            final boolean[] done = {false};
            
            // Run URL check in a separate thread with timeout
            Thread checkerThread = new Thread(() -> {
                try {
                    // Try GET request with a realistic User-Agent to bypass Cloudflare and similar protections
                    HttpRequest getReq = HttpRequest.newBuilder()
                            .uri(URI.create(finalUrl))
                            .timeout(Duration.ofSeconds(25))
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .GET()
                            .build();
                    
                    try {
                        HttpResponse<Void> getResp = client.send(getReq, HttpResponse.BodyHandlers.discarding());
                        int getCode = getResp.statusCode();
                        System.out.println("[URL Checker] GET " + finalUrl + " -> " + getCode);
                        if (getCode >= 200 && getCode < 400) {
                            result[0] = true;
                            done[0] = true;
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("[URL Checker] GET failed for " + finalUrl + ": " + e.getClass().getSimpleName());
                    }
                    
                    // If GET failed, try HEAD request as fallback
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(finalUrl))
                            .timeout(Duration.ofSeconds(20))
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .method("HEAD", HttpRequest.BodyPublishers.noBody())
                            .build();

                    try {
                        HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
                        int code = resp.statusCode();
                        System.out.println("[URL Checker] HEAD " + finalUrl + " -> " + code);
                        result[0] = (code >= 200 && code < 400);
                    } catch (Exception e) {
                        System.out.println("[URL Checker] HEAD failed for " + finalUrl + ": " + e.getClass().getSimpleName());
                        result[0] = false;
                    }
                } catch (Exception e) {
                    System.out.println("[URL Checker] Exception: " + e.getMessage());
                    result[0] = false;
                } finally {
                    done[0] = true;
                }
            });
            
            checkerThread.setDaemon(true);
            checkerThread.start();
            
            // Wait for up to 60 seconds for the check to complete
            long startTime = System.currentTimeMillis();
            while (!done[0] && (System.currentTimeMillis() - startTime) < 60000) {
                Thread.sleep(100);
            }
            
            if (!done[0]) {
                System.out.println("[URL Checker] Timeout for URL: " + url);
                return false;
            }
            
            return result[0];
        } catch (Exception e) {
            System.out.println("[URL Checker] Error in isReachable: " + e.getMessage());
            return false;
        }
    }
}


