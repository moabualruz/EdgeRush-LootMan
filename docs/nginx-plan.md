# nginx Reverse Proxy Plan

Goal: provide a single ingress point for EdgeRush LootMan services (data sync API, dashboards, future web UI) with TLS termination and clean routing.

## Overview
- Deploy nginx in Docker (compose service `nginx`) alongside data-sync and future applications.
- Use `server_name` (e.g., `lootman.local`) with HTTPS (self-signed for dev, proper cert in prod via Let's Encrypt / cert-manager).
- Proxy routes:
  - `/api/sync` → data-sync service (Spring Boot) running on internal port 8080.
  - `/dashboard` → static frontend or future Flutter web app.
  - `/metrics` → Prometheus/Actuator endpoints secured via basic auth.
- Support WebSocket upgrades if needed (future live dashboards).

## docker-compose snippet
```yaml
services:
  nginx:
    image: nginx:1.27
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./deploy/nginx/conf.d:/etc/nginx/conf.d
      - ./deploy/nginx/certs:/etc/nginx/certs
    depends_on:
      - data-sync-service
```

## Sample config (`deploy/nginx/conf.d/lootman.conf`)
```nginx
server {
  listen 80;
  server_name lootman.local;

  location /api/sync/ {
    proxy_pass http://data-sync-service:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  }

  location /metrics {
    proxy_pass http://data-sync-service:8080/actuator/prometheus;
    allow 192.168.0.0/16;
    deny all;
  }
}
```

## Next Steps
1. Add nginx service to `docker-compose.yml` once the API is exposed.
2. Generate self-signed certs for local HTTPS (`mkcert lootman.local`).
3. Update docs with instructions for `/etc/hosts` entries (e.g., `127.0.0.1 lootman.local`).
4. In production, consider using Kubernetes + Ingress controller or managed load balancer.
