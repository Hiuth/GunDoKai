# OUTLINE THUYẾT TRÌNH: TRIỂN KHAI HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ LÊN CLOUD

> **Tổng thời gian**: 10 phút (6 phút slides + 4 phút demo)

---

## SLIDE 1: TRANG BÌA (15 giây)

**Nội dung slide:**
- Tên đề tài: **Triển Khai Hệ Thống Thương Mại Điện Tử Lên Cloud**
- Tên hệ thống: GunDoKai
- Môn học: Điện Toán Đám Mây
- GVHD: [Tên giảng viên]
- Nhóm: [Tên nhóm/thành viên]

**Lời thuyết trình:**
> "Xin chào thầy/cô và các bạn. Hôm nay nhóm em xin trình bày đề tài Triển khai hệ thống thương mại điện tử GunDoKai lên Cloud."

---

## SLIDE 2: GIỚI THIỆU HỆ THỐNG (30 giây)

**Nội dung slide:**
- **GunDoKai**: Hệ thống bán hàng thời trang trực tuyến
- **Backend**: Spring Boot 3.5 + Java 21 + MySQL
- **Frontend**: Next.js (deploy trên Vercel)
- **Tính năng chính**: Quản lý sản phẩm, giỏ hàng, thanh toán VNPay

**Lời thuyết trình:**
> "GunDoKai là hệ thống bán hàng thời trang được xây dựng với Spring Boot cho backend và Next.js cho frontend. Hệ thống hỗ trợ các tính năng cơ bản của e-commerce như quản lý sản phẩm, giỏ hàng và tích hợp thanh toán VNPay."

---

## SLIDE 3: KIẾN TRÚC TRIỂN KHAI (1 phút)

**Nội dung slide:**
- Sơ đồ kiến trúc (từ báo cáo hoặc vẽ mới):
  - User → Cloudflare → AWS EC2 (Docker containers) 
  - Frontend trên Vercel → gọi API qua Cloudflare

**Lời thuyết trình:**
> "Đây là kiến trúc triển khai của hệ thống. Người dùng truy cập qua Cloudflare - đóng vai trò CDN và bảo mật. Request được chuyển đến AWS EC2 instance tại Singapore, nơi chạy các Docker containers. Frontend được deploy riêng trên Vercel và gọi API thông qua Cloudflare."

---

## SLIDE 4: HẠ TẦNG CLOUD - AWS EC2 (45 giây)

**Nội dung slide:**
- Screenshot AWS EC2 Console
- Thông số:
  - Instance type: **c7i-flex.large**
  - Region: **Singapore (ap-southeast-1)**
  - vCPU: 2 | RAM: 4GB | Storage: 30GB SSD
  - OS: Ubuntu 22.04 LTS

**Lời thuyết trình:**
> "Nhóm sử dụng AWS EC2 với instance type c7i-flex.large, đặt tại region Singapore để tối ưu độ trễ cho người dùng Việt Nam. Cấu hình gồm 2 vCPU, 4GB RAM và 30GB SSD, chạy Ubuntu 22.04."

---

## SLIDE 5: CONTAINER HÓA VỚI DOCKER (45 giây)

**Nội dung slide:**
- Bảng 3 containers:

| Container | Image | Chức năng |
|-----------|-------|-----------|
| gundokai-nginx | nginx:alpine | Reverse proxy, SSL |
| gundokai-backend | Custom image | Spring Boot API |
| gundokai-mysql | mysql:8.0 | Database |

- Docker Compose orchestration
- Health check + Auto restart

**Lời thuyết trình:**
> "Toàn bộ hệ thống được container hóa với Docker, gồm 3 containers: Nginx làm reverse proxy, Backend chạy Spring Boot, và MySQL cho database. Docker Compose quản lý việc khởi động theo đúng thứ tự và tự động restart khi có sự cố."

---

## SLIDE 6: BẢO MẬT 5 LỚP (1 phút)

**Nội dung slide:**
- Sơ đồ Defense in Depth (vẽ từ Lucidchart)
- 5 lớp từ ngoài vào trong:
  1. Cloudflare (DDoS, WAF)
  2. UFW Firewall (Port filtering)
  3. Nginx (Rate limiting)
  4. Spring Security (JWT, CORS)
  5. Container Isolation (Docker network)

**Lời thuyết trình:**
> "Hệ thống áp dụng mô hình bảo mật nhiều lớp. Lớp ngoài cùng là Cloudflare chống DDoS và lọc bot. Tiếp theo là UFW firewall chỉ mở port cần thiết. Nginx thực hiện rate limiting. Spring Security xử lý xác thực JWT và CORS. Cuối cùng, Docker network cô lập các container, MySQL chỉ truy cập được từ nội bộ."

---

## SLIDE 7: CLOUDFLARE - CDN & SSL (30 giây)

**Nội dung slide:**
- Screenshot Cloudflare DNS (A record api.xxx)
- Screenshot SSL/TLS settings (Full strict)
- Các tính năng:
  - Proxy mode: Ẩn IP thật của server
  - Origin CA: Mã hóa từ Cloudflare đến VPS
  - Auto HTTPS redirect

**Lời thuyết trình:**
> "Cloudflare được cấu hình với chế độ Proxy để ẩn IP thật của server. SSL mode Full Strict đảm bảo mã hóa end-to-end từ người dùng đến VPS. Domain api.gundokai được trỏ về EC2 instance thông qua A record."

---

## SLIDE 8: QUY TRÌNH DEPLOY (30 giây)

**Nội dung slide:**
- Flow đơn giản:
  ```
  Code → Docker Build → Push DockerHub → Pull trên VPS → Recreate Container
  ```
- Versioning: tag v1.0, v1.1, latest
- Zero-downtime update

**Lời thuyết trình:**
> "Quy trình deploy gồm 4 bước: Build Docker image trên máy local, push lên DockerHub với version tag, sau đó SSH vào VPS pull image mới và recreate container. Việc này chỉ mất vài giây downtime."

---

## SLIDE 9: DEMO (4 phút)

**Nội dung slide:**
- Tiêu đề: **DEMO TRỰC TIẾP**
- Checklist:
  - [ ] Terminal: docker ps, docker stats, docker logs
  - [ ] Browser: Frontend → Login → Xem sản phẩm
  - [ ] Network tab: API calls

**Script demo:**

### Phần 1 - Terminal (1.5 phút):
> "Bây giờ em sẽ SSH vào VPS để kiểm tra hệ thống."

```bash
# Kiểm tra containers đang chạy
docker ps

# Xem resource usage
docker stats --no-stream

# Xem logs backend
docker compose logs --tail=10 backend
```

> "Như các thầy/cô thấy, cả 3 containers đều đang chạy bình thường. CPU và RAM usage ở mức ổn định."

### Phần 2 - Browser (2 phút):
> "Tiếp theo em sẽ demo hệ thống từ phía người dùng."

- Mở frontend: https://gundokai-fe.vercel.app
- Thao tác: Xem trang chủ → Xem sản phẩm → Login
- Mở DevTools → Network tab → Chỉ ra API calls đến api.xxx

> "Đây là frontend deploy trên Vercel. Khi thực hiện các thao tác, các bạn có thể thấy trong Network tab, các request đều gọi đến API backend qua domain api.gundokai."

### Phần 3 - Health check (30 giây):
```bash
curl http://localhost:8080/actuator/health
```
> "Endpoint health check trả về status UP, cho thấy backend và database đều hoạt động bình thường."

---

## SLIDE 10: KẾT LUẬN (30 giây)

**Nội dung slide:**

**Kết quả đạt được:**
- ✅ Triển khai thành công hệ thống lên AWS EC2
- ✅ Container hóa với Docker Compose
- ✅ Bảo mật nhiều lớp (Cloudflare + Firewall + Spring Security)
- ✅ SSL/HTTPS hoạt động ổn định

**Hạn chế:**
- Chưa có hệ thống monitoring chuyên dụng
- Backup database thủ công

**Hướng phát triển:**
- Tích hợp Prometheus + Grafana
- CI/CD với GitHub Actions
- Auto-scaling

**Lời thuyết trình:**
> "Tóm lại, nhóm đã triển khai thành công hệ thống lên AWS với Docker và bảo mật nhiều lớp. Một số hạn chế là chưa có monitoring và backup tự động. Hướng phát triển tiếp theo là tích hợp Prometheus Grafana và CI/CD pipeline. Em xin cảm ơn thầy/cô và các bạn đã lắng nghe."

---

## SLIDE 11: Q&A

**Nội dung slide:**
- **CẢM ƠN THẦY/CÔ VÀ CÁC BẠN ĐÃ LẮNG NGHE**
- Q&A

---

# CHECKLIST CHUẨN BỊ

## Screenshots cần chụp:
- [ ] AWS Console → EC2 Instances
- [ ] Cloudflare → DNS Records
- [ ] Cloudflare → SSL/TLS Settings
- [ ] (Optional) Cloudflare → Analytics

## Sơ đồ cần vẽ (Lucidchart):
- [ ] Kiến trúc triển khai
- [ ] Defense in Depth (5 lớp bảo mật)

## Terminal commands (copy sẵn):
```bash
docker ps
docker stats --no-stream
docker compose logs --tail=10 backend
curl http://localhost:8080/actuator/health
```

## Accounts cần đăng nhập sẵn:
- [ ] AWS Console
- [ ] Cloudflare
- [ ] SSH session (trong terminal)
- [ ] Frontend website (trong browser)
