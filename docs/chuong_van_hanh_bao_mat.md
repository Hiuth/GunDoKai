# CHƯƠNG: VẬN HÀNH, BẢO MẬT & ỨNG BIẾN HỆ THỐNG

## 1. Cơ Chế Vận Hành Hệ Thống Trong Môi Trường Container

### 1.1 Kiến trúc triển khai

Hệ thống GunDoKai được triển khai trên nền tảng **AWS EC2** (region Singapore) với cấu hình **c7i-flex.large** (2 vCPU, 4GB RAM, 30GB SSD). Toàn bộ các thành phần được đóng gói và vận hành trong Docker container, đảm bảo tính nhất quán giữa môi trường phát triển và sản xuất.

**Sơ đồ kiến trúc hệ thống:**

```
                         Internet
                            │
                     Cloudflare (CDN/SSL)
                            │
┌───────────────────────────│───────────────────────────────────────┐
│                    AWS EC2 Instance                                │
│               c7i-flex.large | Singapore                           │
│  ┌──────────────────────────────────────────────────────────────┐ │
│  │                    Docker Engine                              │ │
│  │                                                               │ │
│  │   ┌─────────┐      ┌──────────────┐      ┌─────────────┐    │ │
│  │   │  Nginx  │ ───▶ │   Backend    │ ───▶ │    MySQL    │    │ │
│  │   │ :80/443 │      │ Spring Boot  │      │   :3306     │    │ │
│  │   └─────────┘      └──────────────┘      └──────┬──────┘    │ │
│  │                                                  │           │ │
│  │                    gundokai-network              │           │ │
│  └──────────────────────────────────────────────────│───────────┘ │
│                                              mysql_data (Volume)   │
└────────────────────────────────────────────────────────────────────┘
```

Kiến trúc bao gồm 3 container: **Nginx** (reverse proxy, SSL), **Backend** (Spring Boot API), và **MySQL** (database). Các container giao tiếp qua Docker internal network, chỉ Nginx expose port ra ngoài.

### 1.2 Docker Compose và Container Orchestration

Hệ thống sử dụng **Docker Compose** để quản lý các container với các đặc điểm:

| Tính năng | Mô tả |
|-----------|-------|
| Dependency Management | Backend chờ MySQL healthy trước khi khởi động |
| Health Check | MySQL được kiểm tra mỗi 10 giây bằng `mysqladmin ping` |
| Restart Policy | `unless-stopped` - tự động restart khi crash hoặc VPS reboot |
| Internal Network | Các container giao tiếp qua bridge network riêng biệt |

### 1.3 Multi-stage Build

Dockerfile sử dụng kỹ thuật **multi-stage build**: stage đầu dùng Maven để build JAR, stage sau chỉ copy JAR vào Alpine-based JRE image. Kết quả là image production nhỏ gọn (~200MB), tăng tốc độ pull và deploy.

---

## 2. Cơ Chế Cập Nhật Ứng Dụng và Quản Lý Phiên Bản

### 2.1 Quy trình cập nhật

Quy trình cập nhật được thực hiện qua 2 bước:

1. **Trên máy Local**: Build Docker image với version tag mới (vd: `v1.1`), push lên DockerHub
2. **Trên VPS**: Pull image mới và recreate container với lệnh `docker compose up -d --force-recreate`

### 2.2 Chiến lược versioning

Hệ thống áp dụng **Semantic Versioning** kết hợp với tag `latest`:

| Tag | Mục đích |
|-----|----------|
| Version tag (`v1.0`, `v1.1`) | Đánh dấu release cụ thể, hỗ trợ rollback |
| Latest tag | Luôn trỏ đến bản mới nhất, thuận tiện cho deployment |

### 2.3 Zero-downtime update

Lệnh `--force-recreate` tạo container mới trước khi dừng container cũ, giảm downtime xuống vài giây. Rollback thực hiện bằng cách chỉ định version tag cũ và redeploy.

---

## 3. Quản Lý Cấu Hình và Biến Môi Trường Trong Vận Hành

### 3.1 Tách biệt cấu hình theo môi trường

| Môi trường | File cấu hình | Đặc điểm |
|------------|---------------|----------|
| Local | `docker-compose.local.yml` | Inline env vars, port exposed |
| Production | `docker-compose.yml` + `.env` | Sensitive data trong file riêng |

### 3.2 Các nhóm biến môi trường

| Nhóm | Biến | Mô tả |
|------|------|-------|
| Database | `MYSQL_*`, `SPRING_DATASOURCE_*` | Kết nối MySQL |
| Security | `JWT_SIGNERKEY`, `JWT_*_DURATION` | Cấu hình JWT token |
| External | SMTP, Cloudinary credentials | Dịch vụ bên ngoài |
| Docker | `DOCKER_USER`, `TAG` | Registry và versioning |

File `.env` được lưu tại `/opt/gundokai/` trên VPS với quyền truy cập hạn chế, **không commit vào Git**. Docker Compose tự động inject các biến này vào container khi khởi động.

---

## 4. Cơ Chế Giám Sát và Phát Hiện Sự Cố

### 4.1 Các công cụ giám sát

Hệ thống sử dụng các công cụ tích hợp sẵn của Docker:

| Công cụ | Chức năng |
|---------|-----------|
| `docker logs` | Xem log realtime của từng container |
| `docker stats` | Giám sát CPU, Memory, Network I/O |
| `docker ps` | Kiểm tra trạng thái container |

### 4.2 Health check tự động

MySQL container được cấu hình health check với interval 10 giây. Nếu thất bại liên tiếp 5 lần, container được đánh dấu `unhealthy`, giúp phát hiện sự cố sớm.

### 4.3 Giám sát tầng network

**Cloudflare Analytics** cung cấp thông tin về lưu lượng truy cập, threats blocked, SSL status, và error rate mà không cần cài đặt thêm công cụ.

**Spring Boot Actuator** cung cấp endpoint `/actuator/health` để kiểm tra trạng thái ứng dụng và kết nối database.

---

## 5. Cơ Chế Khôi Phục Hệ Thống Khi Xảy Ra Sự Cố

### 5.1 Khôi phục tự động

Với policy `restart: unless-stopped`, Docker daemon tự động:
- Khởi động lại container khi crash
- Khởi động container sau khi VPS reboot
- Duy trì thứ tự khởi động đúng (MySQL → Backend → Nginx)

### 5.2 Khôi phục thủ công

| Tình huống | Giải pháp | Thời gian |
|------------|-----------|-----------|
| Container crash | Auto restart bởi Docker | ~10 giây |
| VPS reboot | Docker service auto-start | ~1 phút |
| Bad deployment | Rollback về version cũ | ~30 giây |
| Service issue | Restart container cụ thể | ~20 giây |

### 5.3 Data Persistence

- **Database**: MySQL data được lưu trong Docker named volume `mysql_data`, đảm bảo dữ liệu không mất khi container bị xóa.
- **Static assets**: Hình ảnh được lưu trên **Cloudinary** (cloud storage), không phụ thuộc vào VPS.
- **Configuration**: File `.env` và nginx config được backup thủ công định kỳ.

---

## 6. Cơ Chế Bảo Mật Nhiều Lớp Của Hệ Thống

Hệ thống áp dụng mô hình **Defense in Depth** với 5 lớp bảo mật:

**Sơ đồ các lớp bảo mật:**

```
                    Internet
                        │
    ┌───────────────────┼───────────────────┐
    │  LAYER 1: Cloudflare                  │
    │  DDoS Protection, WAF, SSL Edge       │
    └───────────────────┼───────────────────┘
                        │
    ┌───────────────────┼───────────────────┐
    │  LAYER 2: UFW Firewall                │
    │  Only ports 22, 80, 443 open          │
    └───────────────────┼───────────────────┘
                        │
    ┌───────────────────┼───────────────────┐
    │  LAYER 3: Nginx Proxy                 │
    │  Rate Limiting, Origin SSL            │
    └───────────────────┼───────────────────┘
                        │
    ┌───────────────────┼───────────────────┐
    │  LAYER 4: Spring Security             │
    │  JWT Auth, CORS, BCrypt Password      │
    └───────────────────┼───────────────────┘
                        │
    ┌───────────────────┼───────────────────┐
    │  LAYER 5: Container Isolation         │
    │  Docker Network, Internal MySQL       │
    └───────────────────┴───────────────────┘
```

### 6.1 Chi tiết từng lớp

| Lớp | Công nghệ | Chức năng bảo vệ |
|-----|-----------|------------------|
| 1 | Cloudflare | Chống DDoS, ẩn IP server, mã hóa edge, lọc bot |
| 2 | UFW | Chỉ mở port 22/80/443, chặn traffic không mong muốn |
| 3 | Nginx | Rate limiting (10 req/s), SSL nội bộ với Origin CA |
| 4 | Spring Security | JWT authentication, CORS whitelist, BCrypt (cost 10) |
| 5 | Docker | Container isolation, MySQL chỉ truy cập internal |

### 6.2 Bảo mật dữ liệu

- **Mật khẩu**: Hash bằng BCrypt, không lưu plaintext
- **JWT Token**: Ký bằng secret key, có thời hạn và có thể refresh
- **Database**: Chỉ truy cập được từ internal Docker network
- **Credentials**: Lưu trong file `.env`, không commit vào Git

---

## Tổng Kết

| Khía cạnh | Giải pháp áp dụng |
|-----------|-------------------|
| Container Orchestration | Docker Compose với health check và restart policy |
| Version Management | Semantic versioning + Docker image tags |
| Configuration | Environment file separation, Docker env injection |
| Monitoring | Docker native tools + Cloudflare Analytics |
| Recovery | Auto-restart, volume persistence, quick rollback |
| Security | 5-layer defense: Cloudflare → Firewall → Proxy → App → Container |

Kiến trúc này đảm bảo hệ thống vận hành ổn định, dễ bảo trì, và có khả năng chống chịu tốt trước các sự cố cũng như các mối đe dọa bảo mật.
