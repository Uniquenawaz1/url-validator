# Production Deployment Guide - URL Validator

## Quick Start - Deploy with Docker

### Prerequisites
- Docker installed and running
- Internet access to pull from GitHub Container Registry

### Option 1: Run Locally with Docker (for testing)

```bash
# Pull the latest image
docker pull ghcr.io/uniquenawaz1/url-validator:latest

# Run the container
docker run -d \
  --name url-validator \
  -p 8080:8080 \
  ghcr.io/uniquenawaz1/url-validator:latest

# Test the endpoint
curl -X POST http://localhost:8080/api/check-url \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.goindigo.in"}'

# Expected response: {"message":"✅ Valid website URL"}

# Stop and cleanup
docker stop url-validator
docker rm url-validator
```

### Option 2: Deploy to AWS (Recommended for Production)

#### Using AWS Elastic Container Service (ECS)

1. **Create an ECS Task Definition:**
   ```json
   {
     "family": "url-validator",
     "networkMode": "awsvpc",
     "requiresCompatibilities": ["FARGATE"],
     "cpu": "256",
     "memory": "512",
     "containerDefinitions": [
       {
         "name": "url-validator",
         "image": "ghcr.io/uniquenawaz1/url-validator:latest",
         "portMappings": [
           {
             "containerPort": 8080,
             "hostPort": 8080,
             "protocol": "tcp"
           }
         ],
         "logConfiguration": {
           "logDriver": "awslogs",
           "options": {
             "awslogs-group": "/ecs/url-validator",
             "awslogs-region": "us-east-1",
             "awslogs-stream-prefix": "ecs"
           }
         }
       }
     ]
   }
   ```

2. **Create ECS Service:**
   - Create ECS cluster
   - Register the task definition
   - Create a service with 1-3 desired tasks
   - Configure Application Load Balancer (ALB)

3. **Test the deployment:**
   ```bash
   curl -X POST https://your-alb-dns.us-east-1.elb.amazonaws.com/api/check-url \
     -H "Content-Type: application/json" \
     -d '{"url":"https://www.goindigo.in"}'
   ```

### Option 3: Deploy to Docker Hub/Docker Swarm

```bash
# If you want to use Docker Swarm instead
docker service create \
  --name url-validator \
  --publish 8080:8080 \
  ghcr.io/uniquenawaz1/url-validator:latest
```

### Option 4: Deploy to Kubernetes

```bash
# Create a namespace
kubectl create namespace url-validator

# Create a deployment using the manifest below
kubectl apply -f - <<EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: url-validator
  namespace: url-validator
spec:
  replicas: 3
  selector:
    matchLabels:
      app: url-validator
  template:
    metadata:
      labels:
        app: url-validator
    spec:
      containers:
      - name: url-validator
        image: ghcr.io/uniquenawaz1/url-validator:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/health
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: url-validator-service
  namespace: url-validator
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
  selector:
    app: url-validator
EOF

# Test the service
kubectl port-forward -n url-validator svc/url-validator-service 8080:80
# Then test: curl -X POST http://localhost:8080/api/check-url ...
```

### Option 5: Deploy to Heroku

```bash
# Install Heroku CLI
# heroku login

# Create a new Heroku app
heroku create url-validator-app

# Deploy using Docker
# Set the build command to use container registry
heroku container:login
heroku container:push web -a url-validator-app
heroku container:release web -a url-validator-app

# Test
curl -X POST https://url-validator-app.herokuapp.com/api/check-url \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.goindigo.in"}'
```

## Testing the Production Deployment

### 1. Health Check Endpoint
```bash
curl http://your-production-url/api/health
# Expected: {"status":"ok"}
```

### 2. Test URL Validation (Valid URL)
```bash
curl -X POST http://your-production-url/api/check-url \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.google.com"}'
# Expected: {"message":"✅ Valid website URL"}
```

### 3. Test URL Validation (Cloudflare-Protected URL)
```bash
curl -X POST http://your-production-url/api/check-url \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.goindigo.in"}'
# Expected: {"message":"✅ Valid website URL"}
```

### 4. Test URL Validation (Invalid URL)
```bash
curl -X POST http://your-production-url/api/check-url \
  -H "Content-Type: application/json" \
  -d '{"url":"https://invalid-url-that-does-not-exist-12345.com"}'
# Expected: {"message":"❌ Invalid or unreachable website URL"}
```

## Monitoring and Logs

### AWS ECS
- CloudWatch Logs: Check `/ecs/url-validator` log group
- ECS Task Dashboard: Monitor CPU, memory, and network metrics

### Kubernetes
```bash
# View logs
kubectl logs -n url-validator -l app=url-validator --tail=100

# View pod status
kubectl get pods -n url-validator

# Describe pod for troubleshooting
kubectl describe pod <pod-name> -n url-validator
```

## Updating the Production Deployment

When you push changes to the `main` branch:

1. GitHub Actions automatically builds a new Docker image
2. The image is tagged as `ghcr.io/uniquenawaz1/url-validator:latest`
3. Update your deployment to pull the latest image:

```bash
# Docker
docker pull ghcr.io/uniquenawaz1/url-validator:latest
docker stop url-validator
docker rm url-validator
docker run -d --name url-validator -p 8080:8080 ghcr.io/uniquenawaz1/url-validator:latest

# Kubernetes - Trigger rollout
kubectl rollout restart deployment/url-validator -n url-validator

# AWS ECS - Update service to force new task definition
aws ecs update-service --cluster <cluster-name> --service url-validator --force-new-deployment
```

## Troubleshooting

### Container won't start
```bash
# Check logs
docker logs url-validator

# Or for Kubernetes
kubectl logs <pod-name> -n url-validator
```

### Timeout issues connecting to external URLs
- Ensure your network allows outbound HTTPS connections
- Check firewall rules allow port 443 outbound
- The app has 15s connection timeout and 20s read timeout

### SSL/Certificate errors
- The Docker image includes all necessary CA certificates
- If behind a proxy, configure proxy settings in environment

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/check-url` | Check if a URL is reachable |
| GET | `/api/health` | Health check endpoint |
| POST | `/api/check-url-raw` | Debug endpoint (echoes request body) |

### Request Format
```json
{
  "url": "https://www.example.com"
}
```

### Response Format
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

## Performance Characteristics

- **Response time**: 1-5 seconds (depends on target website)
- **Memory usage**: ~256MB-512MB per container
- **CPU usage**: Minimal (< 100m per request)
- **Concurrent users**: Single instance supports ~50+ concurrent requests
- **Recommended replicas**: 3-5 for production high availability

## Version Information

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Base Image**: Alpine Linux (for Docker)
- **Image size**: ~250MB

