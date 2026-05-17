package com.urlvalidator.service;

import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
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

    // Fallback: sites that silently drop HTTP but accept TCP connections
    private boolean isTcpReachable(URI uri) {
        try {
            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : ("https".equals(uri.getScheme()) ? 443 : 80);
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 3000);
                System.out.println("[UrlChecker] TCP reachable: " + host + ":" + port);
                return true;
            }
        } catch (Exception e) {
            System.out.println("[UrlChecker] TCP not reachable: " + e.getMessage());
            return false;
        }
    }

    private boolean isTimeoutException(Exception e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        return e instanceof java.util.concurrent.TimeoutException
                || cause instanceof java.net.http.HttpTimeoutException
                || cause instanceof java.util.concurrent.TimeoutException;
    }

    public boolean isReachable(String url) {
        System.out.println("[UrlChecker.isReachable] Called with: " + url);

        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            System.out.println("[UrlChecker] URL to check: " + url);

            URI uri = new URI(url);

            // Step 1: HEAD request (5s) — fast check
            try {
                HttpRequest request = browserRequest(uri)
                        .timeout(Duration.ofSeconds(5))
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .build();

                HttpResponse<Void> response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                        .get(6, TimeUnit.SECONDS);

                System.out.println("[UrlChecker] HEAD response: " + response.statusCode());

                if (isAcceptedStatus(response.statusCode())) return true;

                // HEAD returned a non-accepted status — try GET (only on bad status, not timeout)
                System.out.println("[UrlChecker] HEAD status " + response.statusCode() + ", trying GET");
                HttpRequest getRequest = browserRequest(uri)
                        .timeout(Duration.ofSeconds(8))
                        .GET()
                        .build();

                HttpResponse<Void> getResponse = httpClient.sendAsync(getRequest, HttpResponse.BodyHandlers.discarding())
                        .get(9, TimeUnit.SECONDS);

                System.out.println("[UrlChecker] GET response: " + getResponse.statusCode());
                if (isAcceptedStatus(getResponse.statusCode())) return true;

            } catch (Exception e) {
                if (!isTimeoutException(e)) {
                    System.out.println("[UrlChecker] Non-timeout exception: " + e.getClass().getSimpleName());
                    // Non-timeout failure (e.g. SSL error, DNS failure at HTTP level) — skip to DNS check
                }
                System.out.println("[UrlChecker] HTTP failed — trying TCP");
            }

            // Step 2: TCP socket check (3s) — for sites that silently drop HTTP
            return isTcpReachable(uri);

        } catch (Exception e) {
            System.out.println("[UrlChecker] Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }
}



