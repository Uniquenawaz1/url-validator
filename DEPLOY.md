# Deploying URL Validator API

This document shows simple ways to host the URL Validator as a standalone API service that other apps can call.

1) Docker (recommended for simplicity)

Build the image locally:

```powershell
cd d:\NZPA\url_validator
docker build -t url-validator:1.0 .
```

Run the container (exposes port 8080):

```powershell
docker run -d -p 8080:8080 --name url-validator url-validator:1.0
```

Now other apps can POST to http://<host>:8080/api/check-url

2) Running on a VM or App Service

- Ensure Java 17 is installed or use the included Docker image.
- Configure environment variable `PORT` if you need a different port.
- Start the jar:

```powershell
java -jar target\url-validator-1.0.0.jar
```

3) Kubernetes / Cloud

- Use the Docker image above and push to your registry:

```powershell
docker tag url-validator:1.0 myregistry/url-validator:1.0
docker push myregistry/url-validator:1.0
```

- Deploy with a Deployment and Service (ClusterIP/LoadBalancer) in your cluster. Expose `/api/*` for API calls.

Security and production notes
- Restrict CORS to only allowed origins instead of `*`.
- Add authentication or API keys if the service will be public.
- Add rate limiting and request validation to prevent abuse (SSRF protection).
- Monitor and add request timeouts via `application.properties`.
