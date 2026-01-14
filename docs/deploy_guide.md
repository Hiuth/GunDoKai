# 🚀 GunDoKai - Hướng Dẫn Deploy Backend

> **Stack**: Spring Boot 3.5.3 + Java 21 + Maven + MySQL  
> **Frontend**: Đã deploy trên Vercel  
> **Target**: Deploy Backend API + MySQL trên VPS với Docker

---

## Tổng Quan Kiến Trúc

```
┌──────────────────────────────────────────────────────────────────────┐
│                           YOUR VPS                                    │
│                                                                       │
│   Internet ──► Cloudflare ──► Nginx (80/443)                          │
│                                   │                                   │
│                                   ▼                                   │
│                           Backend (8080)                              │
│                        [Spring Boot Container]                        │
│                                   │                                   │
│                                   ▼                                   │
│                           MySQL (3306)                                │
│                          [DB Container]                               │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────┐
                    │   Vercel (Frontend) │
                    │        ────►        │
                    │   https://api.xxx   │
                    └─────────────────────┘
```

| Service | Port | Container Name |
|---------|------|----------------|
| Nginx | 80, 443 | gundokai-nginx |
| Backend | 8080 | gundokai-backend |
| MySQL | 3306 | gundokai-mysql |

---

## 📋 Prerequisites

### VPS Requirements
- **OS**: Ubuntu 22.04 LTS
- **RAM**: 2GB+
- **CPU**: 2 cores+
- **Storage**: 20GB+

### Accounts Cần Có
- [ ] DockerHub account (để push images)
- [ ] Cloudflare account (miễn phí)
- [ ] Domain đã trỏ về Cloudflare
- [ ] Gmail với App Password (cho SMTP)
- [ ] Cloudinary account (cho upload hình)

---

## Phase 1: VPS Setup

### 1.1 SSH vào VPS và Update

```bash
ssh root@your-vps-ip

# Update system
apt update && apt upgrade -y

# Install essential packages
apt install -y curl git vim htop ufw
```

### 1.2 Install Docker

```bash
# Install Docker
curl -fsSL https://get.docker.com | sh

# Add current user to docker group
usermod -aG docker $USER

# Enable Docker on startup
systemctl enable docker
systemctl start docker

# Verify
docker --version
docker compose version
```

### 1.3 Configure Firewall

```bash
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
ufw status
```

---

## Phase 2: Tạo Dockerfile cho GunDoKai

### 2.1 Tạo Dockerfile trong project

Tạo file `Dockerfile` tại thư mục gốc của project:

```dockerfile
# ============ BUILD STAGE ============
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml và download dependencies trước (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============ RUNTIME STAGE ============
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy JAR từ build stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Phase 3: Build & Push Docker Image (Trên máy Local)

### 3.1 Login DockerHub

```bash
docker login
# Nhập username và password DockerHub
```

### 3.2 Build & Push Backend

```bash
cd /path/to/GunDoKai

# Build image
docker build --platform linux/amd64 -t your_dockerhub_username/gundokai-backend:v1.0 .

# Push lên DockerHub
docker push your_dockerhub_username/gundokai-backend:v1.0

# Tag latest và push
docker tag your_dockerhub_username/gundokai-backend:v1.0 your_dockerhub_username/gundokai-backend:latest
docker push your_dockerhub_username/gundokai-backend:latest
```

---

## Phase 4: Deploy trên VPS

### 4.1 Tạo thư mục deploy

```bash
# SSH vào VPS
ssh root@your-vps-ip

# Tạo thư mục
mkdir -p /opt/gundokai
cd /opt/gundokai
```

### 4.2 Tạo file .env

```bash
cat > .env <<'EOF'
# === DOCKER ===
DOCKER_USER=your_dockerhub_username
TAG=latest

# === DATABASE ===
MYSQL_DATABASE=gundokai
MYSQL_ROOT_PASSWORD=your_secure_password_here

# === SPRING DATASOURCE ===
SPRING_DATASOURCE_URL=jdbc:mysql://gundokai-mysql:3306/gundokai?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_secure_password_here

# === JWT ===
JWT_SIGNERKEY=your_super_secret_jwt_key_here_make_it_long_and_random
JWT_VALID_DURATION=36000
JWT_REFRESHABLE_DURATION=36000

# === EMAIL (SMTP) ===
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password_16_chars

# === CLOUDINARY ===
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# === SERVER ===
SERVER_PORT=8080
EOF
```

### 4.3 Tạo Docker Compose file

```bash
cat > docker-compose.yml <<'EOF'
version: "3.8"

services:
  # ============ DATABASE ============
  mysql:
    image: mysql:8.0
    container_name: gundokai-mysql
    restart: unless-stopped
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - gundokai-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ============ BACKEND ============
  backend:
    image: ${DOCKER_USER}/gundokai-backend:${TAG:-latest}
    container_name: gundokai-backend
    restart: unless-stopped
    env_file:
      - .env
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - gundokai-network

  # ============ NGINX ============
  nginx:
    image: nginx:alpine
    container_name: gundokai-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    depends_on:
      - backend
    networks:
      - gundokai-network

volumes:
  mysql_data:

networks:
  gundokai-network:
    driver: bridge
EOF
```

### 4.4 Tạo Nginx Configuration

```bash
mkdir -p nginx/ssl

cat > nginx/nginx.conf <<'EOF'
events {
    worker_connections 1024;
}

http {
    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

    # Upstream server
    upstream backend {
        server gundokai-backend:8080;
    }

    # ==== HTTP (redirect to HTTPS) ====
    server {
        listen 80;
        server_name api.yourdomain.com;
        
        location / {
            return 301 https://$host$request_uri;
        }
    }

    # ==== BACKEND API (api.yourdomain.com) ====
    server {
        listen 443 ssl http2;
        server_name api.yourdomain.com;

        # SSL certificates (Cloudflare Origin CA)
        ssl_certificate /etc/nginx/ssl/origin.pem;
        ssl_certificate_key /etc/nginx/ssl/origin.key;

        # SSL settings
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers HIGH:!aNULL:!MD5;

        # Rate limiting
        limit_req zone=api burst=20 nodelay;

        # Larger body for file uploads (50MB như config Spring)
        client_max_body_size 50M;

        location / {
            proxy_pass http://backend;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_cache_bypass $http_upgrade;

            # CORS headers cho Vercel Frontend
            add_header Access-Control-Allow-Origin "https://your-frontend.vercel.app" always;
            add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
            add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
            add_header Access-Control-Allow-Credentials "true" always;

            if ($request_method = OPTIONS) {
                return 204;
            }
        }
    }
}
EOF
```

---

## Phase 5: Cloudflare Configuration

### 5.1 Tạo Origin CA Certificate

1. Đăng nhập **Cloudflare Dashboard**
2. Chọn domain → **SSL/TLS** → **Origin Server**
3. Click **Create Certificate**
4. Chọn:
   - RSA (2048)
   - Hostnames: `*.yourdomain.com`, `yourdomain.com`
   - Validity: 15 years
5. Copy **Origin Certificate** và **Private Key**

### 5.2 Save Certificates trên VPS

```bash
# Paste Origin Certificate
cat > /opt/gundokai/nginx/ssl/origin.pem <<'EOF'
-----BEGIN CERTIFICATE-----
... paste certificate here ...
-----END CERTIFICATE-----
EOF

# Paste Private Key
cat > /opt/gundokai/nginx/ssl/origin.key <<'EOF'
-----BEGIN PRIVATE KEY-----
... paste private key here ...
-----END PRIVATE KEY-----
EOF

# Set permissions
chmod 600 /opt/gundokai/nginx/ssl/origin.key
```

### 5.3 Configure DNS Records

Trong Cloudflare Dashboard → DNS:

| Type | Name | Content | Proxy |
|------|------|---------|-------|
| A | api | your-vps-ip | ☁️ Proxied |

### 5.4 SSL/TLS Settings

Trong Cloudflare → SSL/TLS:
- **SSL mode**: Full (strict)
- **Always Use HTTPS**: ON
- **Minimum TLS Version**: 1.2

---

## Phase 6: Start Deployment

### 6.1 Pull Images và Start

```bash
cd /opt/gundokai

# Login DockerHub (nếu images private)
docker login

# Pull images
docker compose pull

# Start all services
docker compose up -d

# Check status
docker compose ps
docker compose logs -f
```

### 6.2 Verify Deployment

```bash
# Check containers running
docker ps

# Test locally
curl http://localhost:8080/actuator/health

# Check logs
docker compose logs backend
docker compose logs mysql
docker compose logs nginx
```

### 6.3 Test từ Browser

- Backend API: `https://api.yourdomain.com`
- Health Check: `https://api.yourdomain.com/actuator/health`

---

## Phase 7: Connect Frontend (Vercel)

### 7.1 Cập nhật Environment trên Vercel

Trong Vercel Dashboard của Frontend project:

1. Vào **Settings** → **Environment Variables**
2. Thêm hoặc update:

```
NEXT_PUBLIC_API_BASE_URL=https://api.yourdomain.com
```

3. **Redeploy** frontend

### 7.2 Cập nhật CORS trong Nginx

Trong file `nginx/nginx.conf`, thay thế:
```nginx
add_header Access-Control-Allow-Origin "https://your-frontend.vercel.app" always;
```

Thành URL thật của frontend trên Vercel.

### 7.3 Reload Nginx

```bash
docker exec gundokai-nginx nginx -s reload
```

---

## 🔄 Update Deployment

Khi có code mới, thực hiện các bước sau:

### Trên máy Local:

```bash
# 1. Build image mới
docker build --platform linux/amd64 -t your_dockerhub_username/gundokai-backend:v1.1 .

# 2. Push lên DockerHub
docker push your_dockerhub_username/gundokai-backend:v1.1

# 3. Update tag latest
docker tag your_dockerhub_username/gundokai-backend:v1.1 your_dockerhub_username/gundokai-backend:latest
docker push your_dockerhub_username/gundokai-backend:latest
```

### Trên VPS:

```bash
cd /opt/gundokai

# Pull images mới
docker compose pull

# Restart với zero-downtime
docker compose up -d --force-recreate

# Verify
docker compose ps
docker compose logs -f --tail=50
```

---

## 🛠️ Troubleshooting

### Database Connection Error

```bash
# Check database is running
docker compose logs mysql

# Connect to database
docker exec -it gundokai-mysql mysql -u root -p

# Check tables
USE gundokai;
SHOW TABLES;
```

### Nginx Errors

```bash
# Test nginx config
docker exec gundokai-nginx nginx -t

# Check logs
docker compose logs nginx

# Reload config
docker exec gundokai-nginx nginx -s reload
```

### Container Crashes

```bash
# Check logs
docker compose logs <service-name>

# Restart specific service
docker compose restart <service-name>

# Full restart
docker compose down && docker compose up -d
```

### Check Disk Space

```bash
# Check disk usage
df -h

# Clean unused images
docker system prune -a
```

### CORS Issues

Nếu frontend không thể gọi API:
1. Kiểm tra `Access-Control-Allow-Origin` trong nginx config
2. Đảm bảo URL frontend đúng (bao gồm `https://`)
3. Reload nginx sau khi sửa config

---

## 📁 Final Directory Structure

```
/opt/gundokai/
├── docker-compose.yml
├── .env
└── nginx/
    ├── nginx.conf
    └── ssl/
        ├── origin.pem
        └── origin.key
```

---

## ✅ Deployment Checklist

### Before Deploy
- [ ] VPS setup với Docker
- [ ] Firewall configured (80, 443)
- [ ] DockerHub account ready
- [ ] Cloudflare DNS record (api subdomain)
- [ ] Cloudflare Origin CA certificate created
- [ ] Gmail App Password ready
- [ ] Cloudinary credentials ready

### Deploy Steps
- [ ] Dockerfile created in project
- [ ] Backend image built & pushed
- [ ] `.env` configured on VPS
- [ ] `docker-compose.yml` created
- [ ] Nginx config created with CORS
- [ ] SSL certificates saved
- [ ] `docker compose up -d`

### Verify
- [ ] MySQL container healthy
- [ ] Backend container running
- [ ] Nginx container running
- [ ] SSL working (https://api.yourdomain.com)
- [ ] Health check OK
- [ ] Frontend can connect to API

### Connect Frontend
- [ ] Vercel env updated với API URL
- [ ] Frontend redeployed
- [ ] CORS configured với đúng Vercel URL
- [ ] Test login/register từ frontend

---

*Last Updated: January 2026*
