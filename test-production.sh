#!/bin/bash
# Production Deployment Test Script
# This script tests the URL validator in a Docker container

set -e

echo "================================"
echo "URL Validator Production Test"
echo "================================"
echo ""

# Configuration
IMAGE="ghcr.io/uniquenawaz1/url-validator:latest"
CONTAINER_NAME="url-validator-test"
PORT=8080
BASE_URL="http://localhost:$PORT"

# Cleanup function
cleanup() {
    echo ""
    echo "Cleaning up..."
    docker stop $CONTAINER_NAME 2>/dev/null || true
    docker rm $CONTAINER_NAME 2>/dev/null || true
}

# Set trap to cleanup on exit
trap cleanup EXIT

# Pull latest image
echo "1. Pulling latest Docker image..."
docker pull $IMAGE
echo "   ✓ Image pulled successfully"
echo ""

# Run container
echo "2. Starting Docker container..."
docker run -d \
    --name $CONTAINER_NAME \
    -p $PORT:8080 \
    $IMAGE

echo "   ✓ Container started (ID: $(docker ps -q -f name=$CONTAINER_NAME))"
echo ""

# Wait for container to be ready
echo "3. Waiting for app to start..."
for i in {1..30}; do
    if curl -s "$BASE_URL/api/health" > /dev/null 2>&1; then
        echo "   ✓ App is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "   ✗ Timeout waiting for app to start"
        docker logs $CONTAINER_NAME
        exit 1
    fi
    echo -n "."
    sleep 1
done
echo ""

# Test health endpoint
echo "4. Testing health endpoint..."
RESPONSE=$(curl -s "$BASE_URL/api/health")
if echo "$RESPONSE" | grep -q "ok"; then
    echo "   ✓ Health check passed"
    echo "   Response: $RESPONSE"
else
    echo "   ✗ Health check failed"
    echo "   Response: $RESPONSE"
    exit 1
fi
echo ""

# Test Google (should be valid)
echo "5. Testing https://www.google.com (should be valid)..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/check-url" \
    -H "Content-Type: application/json" \
    -d '{"url":"https://www.google.com"}')
if echo "$RESPONSE" | grep -q "Valid"; then
    echo "   ✓ Google URL validation passed"
    echo "   Response: $RESPONSE"
else
    echo "   ✗ Google URL validation failed"
    echo "   Response: $RESPONSE"
    exit 1
fi
echo ""

# Test GoIndiGo (Cloudflare-protected, should be valid)
echo "6. Testing https://www.goindigo.in (Cloudflare-protected, should be valid)..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/check-url" \
    -H "Content-Type: application/json" \
    -d '{"url":"https://www.goindigo.in"}')
if echo "$RESPONSE" | grep -q "Valid"; then
    echo "   ✓ GoIndiGo URL validation passed"
    echo "   Response: $RESPONSE"
else
    echo "   ✗ GoIndiGo URL validation failed"
    echo "   Response: $RESPONSE"
    echo "   Note: This may fail if Cloudflare blocks the request"
    exit 1
fi
echo ""

# Test invalid URL (should be invalid)
echo "7. Testing https://invalid-url-that-does-not-exist-12345.com (should be invalid)..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/check-url" \
    -H "Content-Type: application/json" \
    -d '{"url":"https://invalid-url-that-does-not-exist-12345.com"}')
if echo "$RESPONSE" | grep -q "Invalid"; then
    echo "   ✓ Invalid URL correctly identified"
    echo "   Response: $RESPONSE"
else
    echo "   ✗ Invalid URL test failed"
    echo "   Response: $RESPONSE"
    exit 1
fi
echo ""

# Test with URL that needs https prefix
echo "8. Testing github.com (without https prefix)..."
RESPONSE=$(curl -s -X POST "$BASE_URL/api/check-url" \
    -H "Content-Type: application/json" \
    -d '{"url":"github.com"}')
if echo "$RESPONSE" | grep -q "Valid"; then
    echo "   ✓ URL prefix handling works"
    echo "   Response: $RESPONSE"
else
    echo "   ✗ URL prefix handling failed"
    echo "   Response: $RESPONSE"
fi
echo ""

echo "================================"
echo "All tests passed! ✓"
echo "================================"
echo ""
echo "Container details:"
docker inspect $CONTAINER_NAME | grep -E '"Id"|"Image"|"State"' || true
echo ""
echo "Logs:"
docker logs $CONTAINER_NAME | tail -20 || true
