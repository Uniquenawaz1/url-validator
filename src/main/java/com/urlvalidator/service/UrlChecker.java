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
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public boolean isReachable(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            
            // Try GET request with a realistic User-Agent header to bypass Cloudflare protection
            HttpRequest getReq = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .GET()
                    .build();
            
            try {
                HttpResponse<Void> getResp = client.send(getReq, HttpResponse.BodyHandlers.discarding());
                int getCode = getResp.statusCode();
                System.out.println("[URL Check] GET " + url + " -> " + getCode);
                if (getCode >= 200 && getCode < 400) {
                    return true;
                }
            } catch (Exception e) {
                System.out.println("[URL Check] GET failed, trying HEAD: " + e.getClass().getSimpleName());
            }
            
            // Fallback to HEAD if GET doesn't work
            HttpRequest headReq = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            try {
                HttpResponse<Void> headResp = client.send(headReq, HttpResponse.BodyHandlers.discarding());
                int headCode = headResp.statusCode();
                System.out.println("[URL Check] HEAD " + url + " -> " + headCode);
                return (headCode >= 200 && headCode < 400);
            } catch (Exception e) {
                System.out.println("[URL Check] HEAD also failed: " + e.getClass().getSimpleName());
                return false;
            }
        } catch (Exception e) {
            System.out.println("[URL Check] Error checking URL: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }
}



