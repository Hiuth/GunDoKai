# 🚀 PROJECT OPERATION HUB - HƯỚNG DẪN DEPLOY ĐẦY ĐỦ

> **Domain**: `cevvian.space` (Cloudflare)  
> **VPS**: Ubuntu 22.04 LTS - 2 CPU, 3GB RAM  
> **Stack**: NestJS (Backend) + NextJS (Frontend) + PostgreSQL + Nginx

---

# TỔNG QUAN CÁC BƯỚC

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    DEPLOYMENT ROADMAP                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   PHASE 1: CHUẨN BỊ                                                         │
│   ├── Step 1: Chuẩn bị VPS (Docker, user, firewall)                        │
│   ├── Step 2: Cấu hình Cloudflare DNS                                       │
│   └── Step 3: Chuẩn bị credentials (OAuth, SMTP, secrets)                  │
│                                                                             │
│   PHASE 2: DEPLOY SERVICES                                                  │
│   ├── Step 4: Deploy PostgreSQL Database                                    │
│   ├── Step 5: Build & Deploy Backend (NestJS)                               │
│   ├── Step 6: Build & Deploy Frontend (NextJS)                              │
│   └── Step 7: Deploy Nginx + SSL                                            │
│                                                                             │
│   PHASE 3: TÍCH HỢP & HOÀN THIỆN                                            │
│   ├── Step 8: (Optional) Deploy Jenkins                                     │
│   ├── Step 9: Kiểm tra toàn bộ hệ thống                                     │
│   └── Step 10: Backup & Monitoring                                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

# PHASE 1: CHUẨN BỊ

---

## Step 1: Chuẩn bị VPS

- **Vấn đề có thể gặp**: SSH bị block, Docker cài không thành công, permission denied
- **Kết quả mong đợi**: Docker + Docker Compose hoạt động, user deploy có quyền chạy docker
- **Kiến thức cần nắm**: SSH, Linux basic commands, Docker concepts

### 1.1 SSH vào VPS

```bash
ssh root@103.173.155.185
```

### 1.2 Update hệ thống

```bash
apt update && apt upgrade -y
apt install -y curl wget git vim htop net-tools ufw fail2ban
```

### 1.3 Cài Docker

```bash
# Cài Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Cài Docker Compose plugin
apt install -y docker-compose-plugin

# Verify
docker --version        # Docker version 24.x
docker compose version  # Docker Compose version v2.x
```

### 1.4 Tạo user deploy

```bash
# Tạo user (không dùng root cho apps)
adduser deploy
# Nhập password khi được hỏi

# Thêm quyền
usermod -aG sudo deploy
usermod -aG docker deploy
```

### 1.5 Tạo structure thư mục

```bash
mkdir -p /home/deploy/apps/{database,backend,frontend,nginx,jenkins}
chown -R deploy:deploy /home/deploy/apps
```

### 1.6 Tạo Docker network

```bash
docker network create app-network
```

### 1.7 Cấu hình Firewall

```bash
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp     # SSH
ufw allow 80/tcp     # HTTP
ufw allow 443/tcp    # HTTPS
ufw enable

# Kiểm tra
ufw status
```

### 1.8 (Optional) Cấu hình SSH key-only

```bash
# Trên máy local, copy public key lên server
ssh-copy-id deploy@103.173.155.185

# Trên server, disable password login
vim /etc/ssh/sshd_config
# PasswordAuthentication no

systemctl restart sshd
```

---

## Step 2: Cấu hình Cloudflare DNS

- **Vấn đề có thể gặp**: DNS chưa propagate, SSL mode sai, cache conflict
- **Kết quả mong đợi**: Domain trỏ đúng IP, SSL hoạt động
- **Kiến thức cần nắm**: DNS records, Cloudflare Proxy, SSL modes

### 2.1 Đăng nhập Cloudflare

Truy cập: https://dash.cloudflare.com → Chọn domain `cevvian.space`

### 2.2 Thêm DNS Records

Vào **DNS** → **Records** → **Add record**

| Type | Name | Content | Proxy status | TTL |
|------|------|---------|--------------|-----|
| A | `@` | `103.173.155.185` | Proxied (☁️) | Auto |
| A | `api` | `103.173.155.185` | Proxied (☁️) | Auto |
| A | `jenkins` | `103.173.155.185` | DNS only (☁️→🔘) | Auto |

> **Lưu ý**: Jenkins nên để **DNS only** để webhook từ GitHub hoạt động đúng IP.

### 2.3 Cấu hình SSL/TLS

Vào **SSL/TLS** → **Overview**:
- Chọn: **Full (strict)**

Vào **SSL/TLS** → **Edge Certificates**:
- ✅ Always Use HTTPS: ON
- ✅ Automatic HTTPS Rewrites: ON

### 2.4 (Optional) Page Rules

Vào **Rules** → **Page Rules**:

```
URL: *cevvian.space/*
Setting: SSL = Full (strict)
```

### 2.5 Kiểm tra DNS

```bash
# Chờ 1-5 phút rồi kiểm tra
dig cevvian.space +short
dig api.cevvian.space +short
# Nếu dùng Cloudflare Proxy, sẽ thấy IP của Cloudflare, không phải VPS
```

---

## Step 3: Chuẩn bị Credentials

- **Vấn đề có thể gặp**: OAuth callback URL sai, SMTP blocked, weak secrets
- **Kết quả mong đợi**: Có đầy đủ các credentials cần thiết
- **Kiến thức cần nắm**: OAuth flow, JWT concepts, App passwords

### 3.1 GitHub OAuth App

1. Vào https://github.com/settings/developers
2. **OAuth Apps** → **New OAuth App**

```
Application name: Project Operation Hub
Homepage URL: https://cevvian.space
Authorization callback URL: https://api.cevvian.space/auth/github/callback
```

3. Lưu lại:
   - `GITHUB_CLIENT_ID`
   - `GITHUB_CLIENT_SECRET`

### 3.2 Google OAuth App

1. Vào https://console.cloud.google.com/apis/credentials
2. **Create Credentials** → **OAuth 2.0 Client IDs**

```
Application type: Web application
Name: Project Operation Hub
Authorized redirect URIs: https://api.cevvian.space/auth/google/redirect
```

3. Lưu lại:
   - `GOOGLE_CLIENT_ID`
   - `GOOGLE_CLIENT_SECRET`

### 3.3 Gmail App Password (SMTP)

1. Vào https://myaccount.google.com/security
2. Bật **2-Step Verification**
3. Vào **App passwords** → Tạo password cho "Mail"
4. Lưu lại password 16 ký tự

### 3.4 Generate Secrets

```bash
# Tạo các secrets ngẫu nhiên
openssl rand -base64 32  # JWT_ACCESS_TOKEN_SECRET
openssl rand -base64 32  # JWT_REFRESH_TOKEN_SECRET
openssl rand -base64 32  # JWT_EMAIL_VERIFICATION_SECRET
openssl rand -base64 32  # JWT_PASSWORD_RESET_SECRET
openssl rand -base64 24  # GITHUB_WEBHOOK_SECRET
openssl rand -base64 24  # JENKINS_API_KEY
```

### 3.5 Tạo Docker Hub Account

1. Đăng ký tại https://hub.docker.com
2. Tạo **Access Token**: Account Settings → Security → New Access Token
3. Lưu lại username và token

---

# PHASE 2: DEPLOY SERVICES

---

## Step 4: Deploy PostgreSQL Database

- **Vấn đề có thể gặp**: Port conflict, volume permission, connection refused
- **Kết quả mong đợi**: Database chạy, có thể connect từ backend container
- **Kiến thức cần nắm**: Docker volumes, Docker networks, PostgreSQL basics

### 4.1 Tạo files

```bash
# SSH với user deploy
ssh deploy@103.173.155.185
cd ~/apps/database
```

### 4.2 Tạo docker-compose.yml

```yaml
# ~/apps/database/docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: postgres
    restart: unless-stopped
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./backups:/backups
    ports:
      - "127.0.0.1:5432:5432"
    networks:
      - app-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER}"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:

networks:
  app-network:
    external: true
```

### 4.3 Tạo .env

```bash
# ~/apps/database/.env
POSTGRES_USER=admin
POSTGRES_PASSWORD=your_super_strong_password_here
POSTGRES_DB=project_hub
```

### 4.4 Deploy

```bash
cd ~/apps/database
docker compose up -d

# Kiểm tra
docker compose ps
docker compose logs

# Test connection
docker exec -it postgres psql -U admin -d project_hub -c "SELECT 1;"
```

### 4.5 Tạo backup script

```bash
# ~/apps/database/backup.sh
#!/bin/bash
BACKUP_DIR="/home/deploy/apps/database/backups"
DATE=$(date +%Y%m%d_%H%M%S)
docker exec postgres pg_dumpall -U admin | gzip > $BACKUP_DIR/backup_$DATE.sql.gz
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete
echo "Backup completed: backup_$DATE.sql.gz"
```

```bash
chmod +x backup.sh
mkdir -p backups

# Thêm crontab
(crontab -l 2>/dev/null; echo "0 2 * * * /home/deploy/apps/database/backup.sh") | crontab -
```

---

## Step 5: Build & Deploy Backend (NestJS)

- **Vấn đề có thể gặp**: Docker build fail, env variables missing, database connection error
- **Kết quả mong đợi**: Backend API chạy, health check OK
- **Kiến thức cần nắm**: Docker multi-stage build, NestJS config, TypeORM

### 5.1 Trên máy LOCAL: Build Docker image

```bash
cd /Users/trantrongtri/Desktop/Thu/Project_Operation_Hub

# Login Docker Hub
docker login
# Nhập username và access token

# Build image cho Linux (VPS là linux/amd64)
docker build --platform linux/amd64 -t your_dockerhub_username/project-hub-backend:v1 .

# Push lên Docker Hub
docker push your_dockerhub_username/project-hub-backend:v1
```

### 5.2 Trên VPS: Tạo files

```bash
ssh deploy@103.173.155.185
cd ~/apps/backend
```

### 5.3 Tạo docker-compose.yml

```yaml
# ~/apps/backend/docker-compose.yml
version: '3.8'

services:
  backend:
    image: ${DOCKER_USER}/project-hub-backend:${TAG:-latest}
    container_name: backend
    restart: unless-stopped
    env_file:
      - .env
    ports:
      - "127.0.0.1:4000:4000"
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:4000/health"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  app-network:
    external: true
```

### 5.4 Tạo .env

```bash
# ~/apps/backend/.env

# === DOCKER ===
DOCKER_USER=your_dockerhub_username
TAG=v1

# === DATABASE ===
POSTGRES_USER=admin
POSTGRES_PASSWORD=your_super_strong_password_here
POSTGRES_DB=project_hub
DB_HOST=postgres
DB_PORT=5432

# === APPLICATION ===
API_PORT=4000
NODE_ENV=production
APP_FRONTEND_URL=https://cevvian.space

# === GITHUB OAUTH ===
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
GITHUB_CALLBACK_URL=https://api.cevvian.space/auth/github/callback

# === GITHUB INTEGRATION ===
GITHUB_TOKEN=your_github_personal_access_token
GITHUB_ORG=your_github_org
GITHUB_WEBHOOK_SECRET=your_webhook_secret

# === GOOGLE OAUTH ===
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_CALLBACK_URL=https://api.cevvian.space/auth/google/redirect

# === JWT ===
JWT_ACCESS_TOKEN_SECRET=your_generated_secret_1
JWT_REFRESH_TOKEN_SECRET=your_generated_secret_2
JWT_EMAIL_VERIFICATION_SECRET=your_generated_secret_3
JWT_PASSWORD_RESET_SECRET=your_generated_secret_4
JWT_ACCESS_TOKEN_EXPIRATION=15m
JWT_REFRESH_TOKEN_EXPIRATION=7d

# === SMTP ===
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email@gmail.com
SMTP_PASSWORD=your_app_password

# === CLOUDINARY ===
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# === JENKINS ===
JENKINS_URL=https://jenkins.cevvian.space
JENKINS_USER=admin
JENKINS_TOKEN=your_jenkins_token
JENKINS_API_KEY=your_jenkins_api_key
GITHUB_JENKINS_WEBHOOK_SECRET=your_webhook_secret
```

### 5.5 Deploy

```bash
cd ~/apps/backend

# Pull image
docker compose pull

# Start
docker compose up -d

# Kiểm tra logs
docker compose logs -f

# Test health
curl http://localhost:4000/health
```

---

## Step 6: Build & Deploy Frontend (NextJS)

- **Vấn đề có thể gặp**: Build-time env không có, NEXT_PUBLIC_ prefix cần đúng
- **Kết quả mong đợi**: Frontend chạy, có thể gọi API
- **Kiến thức cần nắm**: NextJS build process, environment variables

### 6.1 Cập nhật Dockerfile (nếu cần)

Dockerfile hiện tại của frontend hơi đơn giản. Khuyến nghị dùng multi-stage:

```dockerfile
# ~/project-operation-hub-ui/Dockerfile
FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# Build với env
ARG NEXT_PUBLIC_API_BASE_URL
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL

RUN npm run build

# Production
FROM node:18-alpine

WORKDIR /app

COPY --from=builder /app/package*.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/node_modules ./node_modules

EXPOSE 3000

CMD ["npm", "start"]
```

### 6.2 Trên máy LOCAL: Build Docker image

```bash
cd /Users/trantrongtri/Desktop/Thu/project-operation-hub-ui

# Build với API URL (quan trọng!)
docker build --platform linux/amd64 \
  --build-arg NEXT_PUBLIC_API_BASE_URL=https://api.cevvian.space \
  -t your_dockerhub_username/project-hub-frontend:v1 .

# Push
docker push your_dockerhub_username/project-hub-frontend:v1
```

### 6.3 Trên VPS: Tạo files

```bash
ssh deploy@103.173.155.185
cd ~/apps/frontend
```

### 6.4 Tạo docker-compose.yml

```yaml
# ~/apps/frontend/docker-compose.yml
version: '3.8'

services:
  frontend:
    image: ${DOCKER_USER}/project-hub-frontend:${TAG:-latest}
    container_name: frontend
    restart: unless-stopped
    ports:
      - "127.0.0.1:3000:3000"
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:3000"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  app-network:
    external: true
```

### 6.5 Tạo .env

```bash
# ~/apps/frontend/.env
DOCKER_USER=your_dockerhub_username
TAG=v1
```

### 6.6 Deploy

```bash
cd ~/apps/frontend
docker compose pull
docker compose up -d
docker compose logs -f

# Test
curl http://localhost:3000
```

---

## Step 7: Deploy Nginx + SSL

- **Vấn đề có thể gặp**: SSL certificate fail, port 80 bị chiếm, upstream not found
- **Kết quả mong đợi**: HTTPS hoạt động, route đúng các services
- **Kiến thức cần nắm**: Nginx config, SSL/TLS, Reverse Proxy

### 7.1 Cài Certbot (với root)

```bash
# SSH với root
ssh root@103.173.155.185

# Stop nginx nếu đang chạy (để lấy cert)
docker stop nginx 2>/dev/null || true

# Cài certbot
apt install -y certbot

# Lấy SSL certificate
certbot certonly --standalone \
  -d cevvian.space \
  -d www.cevvian.space \
  -d api.cevvian.space \
  -d jenkins.cevvian.space \
  --email your_email@gmail.com \
  --agree-tos \
  --no-eff-email

# Certificate được lưu tại:
# /etc/letsencrypt/live/cevvian.space/fullchain.pem
# /etc/letsencrypt/live/cevvian.space/privkey.pem
```

### 7.2 Setup auto-renew

```bash
# Thêm crontab cho auto-renew
(crontab -l 2>/dev/null; echo "0 0 1 * * certbot renew --quiet --pre-hook 'docker stop nginx' --post-hook 'docker start nginx'") | crontab -
```

### 7.3 Tạo Nginx config

```bash
# Chuyển sang user deploy
su - deploy
cd ~/apps/nginx
```

### 7.4 Tạo docker-compose.yml

```yaml
# ~/apps/nginx/docker-compose.yml
version: '3.8'

services:
  nginx:
    image: nginx:alpine
    container_name: nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - /etc/letsencrypt:/etc/letsencrypt:ro
    networks:
      - app-network

networks:
  app-network:
    external: true
```

### 7.5 Tạo nginx.conf

```nginx
# ~/apps/nginx/nginx.conf
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    # Logging
    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;
    
    # Gzip
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;
    
    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    
    # HTTP → HTTPS redirect
    server {
        listen 80;
        server_name _;
        return 301 https://$host$request_uri;
    }
    
    # Frontend: cevvian.space
    server {
        listen 443 ssl http2;
        server_name cevvian.space www.cevvian.space;
        
        ssl_certificate /etc/letsencrypt/live/cevvian.space/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/cevvian.space/privkey.pem;
        
        # Security headers
        add_header X-Frame-Options "SAMEORIGIN" always;
        add_header X-Content-Type-Options "nosniff" always;
        add_header X-XSS-Protection "1; mode=block" always;
        
        location / {
            proxy_pass http://frontend:3000;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_cache_bypass $http_upgrade;
        }
    }
    
    # Backend API: api.cevvian.space
    server {
        listen 443 ssl http2;
        server_name api.cevvian.space;
        
        ssl_certificate /etc/letsencrypt/live/cevvian.space/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/cevvian.space/privkey.pem;
        
        # Rate limiting for API
        location / {
            limit_req zone=api burst=20 nodelay;
            
            proxy_pass http://backend:4000;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Larger body for file uploads
            client_max_body_size 50M;
        }
    }
    
    # Jenkins: jenkins.cevvian.space
    server {
        listen 443 ssl http2;
        server_name jenkins.cevvian.space;
        
        ssl_certificate /etc/letsencrypt/live/cevvian.space/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/cevvian.space/privkey.pem;
        
        location / {
            proxy_pass http://jenkins:8080;
            proxy_http_version 1.1;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_set_header X-Forwarded-Host $host;
            proxy_set_header X-Forwarded-Port $server_port;
            
            # Required for Jenkins
            proxy_request_buffering off;
        }
    }
}
```

### 7.6 Deploy

```bash
cd ~/apps/nginx
docker compose up -d
docker compose logs -f
```

### 7.7 Test

```bash
# Test từ VPS
curl -I https://cevvian.space
curl -I https://api.cevvian.space/health

# Test từ browser
# https://cevvian.space
# https://api.cevvian.space/health
```

---

# PHASE 3: TÍCH HỢP & HOÀN THIỆN

---

## Step 8: (Optional) Deploy Jenkins

- **Vấn đề có thể gặp**: Thiếu RAM, Docker-in-Docker setup
- **Kết quả mong đợi**: Jenkins UI accessible, có thể trigger builds
- **Kiến thức cần nắm**: Jenkins architecture, Pipeline concept

### 8.1 Tạo files

```bash
cd ~/apps/jenkins
```

### 8.2 Tạo docker-compose.yml

```yaml
# ~/apps/jenkins/docker-compose.yml
version: '3.8'

services:
  jenkins:
    image: jenkins/jenkins:lts-jdk17
    container_name: jenkins
    restart: unless-stopped
    user: root
    environment:
      - JAVA_OPTS=-Xmx512m -Xms256m
    volumes:
      - jenkins_home:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
      - /usr/bin/docker:/usr/bin/docker
    ports:
      - "127.0.0.1:8080:8080"
      - "127.0.0.1:50000:50000"
    networks:
      - app-network

volumes:
  jenkins_home:

networks:
  app-network:
    external: true
```

### 8.3 Deploy

```bash
docker compose up -d

# Lấy initial password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Truy cập: https://jenkins.cevvian.space
# Nhập password, cài suggested plugins
```

---

## Step 9: Kiểm tra toàn bộ hệ thống

- **Vấn đề có thể gặp**: Service nào đó không chạy, network issues
- **Kết quả mong đợi**: Tất cả services hoạt động, có thể login
- **Kiến thức cần nắm**: Docker troubleshooting, log analysis

### 9.1 Check all containers

```bash
docker ps

# Expected output:
# CONTAINER ID   IMAGE                  STATUS          NAMES
# xxxx           postgres:16-alpine     Up X hours      postgres
# xxxx           xxx/project-hub-be     Up X hours      backend
# xxxx           xxx/project-hub-fe     Up X hours      frontend
# xxxx           nginx:alpine           Up X hours      nginx
# xxxx           jenkins/jenkins:lts    Up X hours      jenkins
```

### 9.2 Check network

```bash
docker network inspect app-network
```

### 9.3 Test endpoints

```bash
# Database (internal only)
docker exec -it postgres psql -U admin -d project_hub -c "SELECT 1;"

# Backend
curl http://localhost:4000/health
curl https://api.cevvian.space/health

# Frontend
curl http://localhost:3000
curl https://cevvian.space

# Full flow test
# 1. Mở https://cevvian.space
# 2. Click Login with GitHub
# 3. Verify redirect và login thành công
```

### 9.4 Check logs nếu có lỗi

```bash
# Database
docker compose -f ~/apps/database/docker-compose.yml logs --tail=50

# Backend
docker compose -f ~/apps/backend/docker-compose.yml logs --tail=50

# Frontend
docker compose -f ~/apps/frontend/docker-compose.yml logs --tail=50

# Nginx
docker compose -f ~/apps/nginx/docker-compose.yml logs --tail=50
```

---

## Step 10: Backup & Monitoring

- **Vấn đề có thể gặp**: Disk full, backup fail
- **Kết quả mong đợi**: Có backup định kỳ, có thể monitor resources
- **Kiến thức cần nắm**: Crontab, system monitoring

### 10.1 Setup backup cron (đã làm ở Step 4)

```bash
# Verify crontab
crontab -l

# Expected:
# 0 2 * * * /home/deploy/apps/database/backup.sh
# 0 0 1 * * certbot renew ...
```

### 10.2 Monitoring commands

```bash
# Resource usage
docker stats

# Disk space
df -h
docker system df

# Memory
free -h

# Logs size
du -sh /var/lib/docker/containers/*
```

### 10.3 Cleanup commands

```bash
# Xóa old images
docker image prune -af

# Xóa old containers
docker container prune -f

# Xóa everything unused
docker system prune -af
```

---

# 📋 QUICK REFERENCE

## URLs

| Service | URL |
|---------|-----|
| Frontend | https://cevvian.space |
| Backend API | https://api.cevvian.space |
| API Health | https://api.cevvian.space/health |
| Jenkins | https://jenkins.cevvian.space |

## Common Commands

```bash
# SSH
ssh deploy@103.173.155.185

# Restart service
cd ~/apps/backend && docker compose restart

# Update service (after pushing new image)
cd ~/apps/backend
docker compose pull
docker compose up -d

# View logs
docker compose logs -f

# Check all services
docker ps

# Backup database now
~/apps/database/backup.sh
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Container không start | `docker compose logs` để xem lỗi |
| Database connection refused | Check `DB_HOST=postgres` (container name) |
| SSL error | `certbot renew --force-renewal` |
| API 502 Bad Gateway | Backend container chết, `docker compose restart` |
| Frontend blank | Check NEXT_PUBLIC_API_BASE_URL |
| OAuth callback error | Check callback URLs trong GitHub/Google console |

---

# ✅ DEPLOYMENT CHECKLIST

## Trước khi deploy
- [ ] Có VPS với IP public
- [ ] Domain đã cấu hình trên Cloudflare
- [ ] Có Docker Hub account
- [ ] Có GitHub OAuth App credentials
- [ ] Có Google OAuth App credentials
- [ ] Có Gmail App Password cho SMTP
- [ ] Đã generate tất cả secrets

## Sau khi deploy
- [ ] Database container running
- [ ] Backend container running
- [ ] Frontend container running
- [ ] Nginx container running
- [ ] SSL certificate valid
- [ ] Can access https://cevvian.space
- [ ] Can access https://api.cevvian.space/health
- [ ] GitHub OAuth login works
- [ ] Google OAuth login works
- [ ] Backup cron configured
- [ ] Firewall configured