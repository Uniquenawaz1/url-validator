# Production Deployment Options & Comparison

## Quick Reference Table

| Platform | Setup Time | Cost | Scalability | Monitoring | Recommendation |
|----------|-----------|------|-------------|-----------|-----------------|
| **AWS ECS** | 20 min | Low-Medium | ⭐⭐⭐⭐⭐ | Excellent | ✅ Best for production |
| **Kubernetes** | 30 min | Medium | ⭐⭐⭐⭐⭐ | Excellent | ✅ Multi-cloud |
| **Docker Swarm** | 10 min | Low | ⭐⭐⭐ | Good | Good for small teams |
| **Heroku** | 5 min | Medium | ⭐⭐⭐ | Basic | ✅ Fastest to deploy |
| **Local Docker** | 2 min | Free | ⭐ | Good | Testing only |

## Deployment Recommendations by Use Case

### 🏢 Enterprise / Large Scale
**Recommended:** AWS ECS or Kubernetes
- Use multiple replicas (3-5) for high availability
- Enable auto-scaling based on CPU/memory
- Setup CloudWatch alarms for monitoring
- Use Application Load Balancer for traffic distribution

**Setup Time:** 20-30 minutes

```bash
# Example: Deploy 3 replicas with ECS Fargate
# See PRODUCTION_DEPLOYMENT.md for full configuration
```

### 🚀 Startup / Small Team
**Recommended:** Heroku or AWS ECS Fargate
- Minimal infrastructure management
- Auto-scaling handled by platform
- Setup monitoring and alerts
- Focus on application logic

**Setup Time:** 5-10 minutes

```bash
heroku create url-validator-api
heroku container:push web
heroku container:release web
```

### 🌐 Multi-Cloud / Kubernetes
**Recommended:** Kubernetes (AWS EKS, Google GKE, Azure AKS, or self-hosted)
- Maximum flexibility and portability
- Excellent for DevOps teams
- Auto-healing and self-recovery
- Complex but powerful

**Setup Time:** 30-45 minutes

### 🐳 Simple / Self-Hosted
**Recommended:** Docker Compose or Docker Swarm
- Perfect for development/staging
- Single-server deployments
- Easy to understand and manage
- Limited scaling capabilities

**Setup Time:** 10 minutes

## Step-by-Step Deployment Guides

### Option 1: Deploy to Heroku (Fastest - 5 minutes) ⚡

```bash
# 1. Install Heroku CLI
# 2. Login to Heroku
heroku login

# 3. Create new app
heroku create url-validator-prod

# 4. Set up container registry
heroku container:login

# 5. Push Docker image
heroku container:push web -a url-validator-prod

# 6. Release the image
heroku container:release web -a url-validator-prod

# 7. Test
curl https://url-validator-prod.herokuapp.com/api/health

# 8. Monitor logs
heroku logs --tail -a url-validator-prod
```

**Cost:** $50/month (Dyno)
**Scaling:** Manual or automatic dyno scaling
**Monitoring:** Heroku dashboard + add-ons

---

### Option 2: Deploy to AWS ECS (Recommended - 20 minutes) 🎯

```bash
# 1. Create ECS Cluster
aws ecs create-cluster --cluster-name url-validator

# 2. Create Task Definition (use JSON from PRODUCTION_DEPLOYMENT.md)
aws ecs register-task-definition --cli-input-json file://task-definition.json

# 3. Create ECS Service
aws ecs create-service \
  --cluster url-validator \
  --service-name url-validator-service \
  --task-definition url-validator:1 \
  --desired-count 3 \
  --load-balancers targetGroupArn=<ARN>,containerName=url-validator,containerPort=8080

# 4. Setup Application Load Balancer
# (Create through AWS Console or CLI)

# 5. Test
curl http://<ALB-DNS>/api/health

# 6. Monitor
# View CloudWatch Logs and metrics in AWS Console
```

**Cost:** $50-200/month (depending on instance size and data transfer)
**Scaling:** Built-in auto-scaling groups
**Monitoring:** CloudWatch (logs, metrics, dashboards)

---

### Option 3: Deploy to Kubernetes (Powerful - 30 minutes) 🚀

```bash
# 1. Create namespace
kubectl create namespace url-validator

# 2. Create ConfigMap (optional, for environment variables)
kubectl create configmap url-validator-config \
  --from-literal=LOG_LEVEL=INFO \
  -n url-validator

# 3. Deploy application (use manifest from PRODUCTION_DEPLOYMENT.md)
kubectl apply -f deployment.yaml -n url-validator

# 4. Expose service
kubectl expose deployment url-validator \
  --type=LoadBalancer \
  --port=80 \
  --target-port=8080 \
  -n url-validator

# 5. Get external IP
kubectl get svc -n url-validator

# 6. Test
curl http://<EXTERNAL-IP>/api/health

# 7. Monitor
kubectl logs -n url-validator -l app=url-validator --tail=100
kubectl top pods -n url-validator
```

**Cost:** Depends on cluster (AWS EKS $73/month + compute)
**Scaling:** Automatic based on HPA
**Monitoring:** Prometheus, Grafana, or cloud-native monitoring

---

### Option 4: Local Docker (Testing - 2 minutes) 🐳

```bash
# 1. Pull image
docker pull ghcr.io/uniquenawaz1/url-validator:latest

# 2. Run container
docker run -d \
  --name url-validator \
  -p 8080:8080 \
  ghcr.io/uniquenawaz1/url-validator:latest

# 3. Test
curl http://localhost:8080/api/health

# 4. View logs
docker logs -f url-validator

# 5. Stop
docker stop url-validator
```

**Cost:** Free
**Scaling:** Manual only
**Monitoring:** Docker logs command

---

### Option 5: Docker Compose (Small Deployment - 10 minutes) 📦

Create `docker-compose.yml`:
```yaml
version: '3.8'

services:
  url-validator:
    image: ghcr.io/uniquenawaz1/url-validator:latest
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xmx512m
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  # Optional: Add nginx reverse proxy
  nginx:
    image: nginx:latest
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - url-validator
```

```bash
# 1. Create the compose file (as above)

# 2. Start services
docker-compose up -d

# 3. Test
curl http://localhost/api/health

# 4. View logs
docker-compose logs -f url-validator

# 5. Scale up
docker-compose up -d --scale url-validator=3

# 6. Stop
docker-compose down
```

**Cost:** Free (self-hosted)
**Scaling:** Manual scaling
**Monitoring:** Docker logs

---

## Monitoring Setup by Platform

### AWS ECS + CloudWatch
```bash
# Create CloudWatch log group (automatic with ECS)
# Create CloudWatch dashboard
# Set up alarms:
# - HTTP 5xx error rate > 5%
# - Average response time > 5 seconds
# - Container restart count > 0

# View logs
aws logs tail /ecs/url-validator --follow
```

### Kubernetes + Prometheus
```bash
# Install Prometheus
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/prometheus -n url-validator

# Install Grafana
helm repo add grafana https://grafana.github.io/helm-charts
helm install grafana grafana/grafana -n url-validator

# Access Grafana
kubectl port-forward -n url-validator svc/grafana 3000:80
```

### Heroku Dashboard
```bash
# Login to Heroku dashboard
# Navigate to your app
# View real-time logs and metrics
# Set up Papertrail add-on for log management
```

---

## Load Testing Results (Expected)

With a single container instance:
- **Throughput:** ~50 requests/second
- **Avg Response Time:** 2-3 seconds (varies by target URL)
- **P99 Response Time:** 5-10 seconds
- **Memory Usage:** 256-512MB
- **CPU Usage:** 10-50% (depends on URL validation time)

With 3 replicas + load balancer:
- **Throughput:** ~150 requests/second
- **Availability:** 99.9% (with auto-healing)
- **Failover Time:** < 5 seconds

---

## Cost Comparison (Monthly Estimate)

### Scenario: Production with 3 replicas, 1000 requests/day

| Platform | Compute | Storage | Data | Other | **Total** |
|----------|---------|---------|------|-------|-----------|
| AWS ECS Fargate | $40 | $2 | $5 | - | **$47** |
| AWS EC2 (3x t3.small) | $60 | $5 | $10 | - | **$75** |
| Heroku (3x Standard-1X) | $150 | included | included | - | **$150** |
| Kubernetes (AWS EKS) | $73 + compute | $5 | $5 | - | **$83+** |
| Docker Swarm (2x t3.small) | $40 | $5 | $5 | - | **$50** |
| Local Server | $100-500 | included | $50 | Power | **$150-550** |

---

## Deployment Checklist

- [ ] Choose deployment platform
- [ ] Configure environment variables (if needed)
- [ ] Setup monitoring and logging
- [ ] Configure autoscaling policies
- [ ] Setup health checks
- [ ] Configure backup/recovery strategy
- [ ] Setup CI/CD pipeline (if using custom)
- [ ] Test failover scenarios
- [ ] Document runbooks for common issues
- [ ] Setup on-call alerting
- [ ] Deploy to staging first
- [ ] Run full regression tests
- [ ] Deploy to production during low-traffic period
- [ ] Monitor for 24 hours after deployment

---

## Troubleshooting Common Issues

### Container crashes on startup
```bash
# View logs
docker logs <container-id>
# Check memory: usually OOM
# Increase memory allocation
```

### High latency
```bash
# Monitor response times
# Check if target URLs are slow
# May need timeout adjustment in code
# Consider adding request caching
```

### Connection errors to external URLs
```bash
# Verify outbound HTTPS allowed
# Check firewall rules (port 443)
# Verify DNS resolution works
# May need proxy configuration
```

### Memory leaks
```bash
# Monitor memory usage over time
# Check for connection pool leaks
# Enable Java heap dumps
# Restart containers on schedule (if necessary)
```

---

**Choose your deployment platform and follow the step-by-step guide above!** 🚀
