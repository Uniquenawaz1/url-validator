package com.urlvalidator.service;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class UrlChecker {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public UrlChecker() {
        System.out.println("[UrlChecker] Initialized");
    }

    private HttpRequest.Builder browserRequest(URI uri) {
        return HttpRequest.newBuilder()
                .uri(uri)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Upgrade-Insecure-Requests", "1")
                .header("Sec-Fetch-Dest", "document")
                .header("Sec-Fetch-Mode", "navigate")
                .header("Sec-Fetch-Site", "none")
                .header("Sec-Fetch-User", "?1");
    }

    private boolean isAcceptedStatus(int status) {
        // 2xx/3xx = success/redirect; 403/405/429 = blocked but site exists
        return (status >= 200 && status < 400) || status == 403 || status == 405 || status == 429;
    }

    public boolean isReachable(String url) {
        System.out.println("[UrlChecker.isReachable] Called with: " + url);

        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            System.out.println("[UrlChecker] URL to check: " + url);

            URI uri = new URI(url);
            System.out.println("[UrlChecker] URI parsed successfully");

            // Try HEAD first
            HttpRequest request = browserRequest(uri)
                    .timeout(Duration.ofSeconds(10))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            System.out.println("[UrlChecker] Request created, attempting to send...");

            CompletableFuture<HttpResponse<Void>> future = httpClient.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.discarding()
            );

            System.out.println("[UrlChecker] Async request sent");

            HttpResponse<Void> response = future.get(15, TimeUnit.SECONDS);

            System.out.println("[UrlChecker] Got response: " + response.statusCode());

            if (isAcceptedStatus(response.statusCode())) {
                System.out.println("[UrlChecker] ✓ URL is reachable");
                return true;
            }

            // If HEAD returned an unexpected status, try GET
            System.out.println("[UrlChecker] HEAD returned status " + response.statusCode() + ", trying GET");

            HttpRequest getRequest = browserRequest(uri)
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            CompletableFuture<HttpResponse<Void>> getfuture = httpClient.sendAsync(
                    getRequest,
                    HttpResponse.BodyHandlers.discarding()
            );

            HttpResponse<Void> getResponse = getfuture.get(20, TimeUnit.SECONDS);

            System.out.println("[UrlChecker] GET response: " + getResponse.statusCode());

            return isAcceptedStatus(getResponse.statusCode());
            
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



