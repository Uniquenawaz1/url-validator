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
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public boolean isReachable(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> resp = client.send(req, HttpResponse.BodyHandlers.discarding());
            int code = resp.statusCode();
            // Consider 200-399 valid; others indicate site responded but may be restricted
            if (code >= 200 && code < 400) {
                return true;
            }
            // If HEAD returned non-2xx (or e.g. 405 Method Not Allowed), try a GET as a fallback
            HttpRequest getReq = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<Void> getResp = client.send(getReq, HttpResponse.BodyHandlers.discarding());
            int getCode = getResp.statusCode();
            return (getCode >= 200 && getCode < 400);
        } catch (Exception e) {
            // treat any exception (connectivity, SSL, invalid URI) as unreachable
            return false;
        }
    }
}
