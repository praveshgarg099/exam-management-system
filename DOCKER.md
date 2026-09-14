# Exam Management System — All-In-One Container Deployment (Live Web Access)

This setup packages the complete **Exam Management System (EMS)** into a single, self-contained Docker container that includes:
1. **PostgreSQL Database** (auto-configured and persistent)
2. **Java 21 JRE Runtime** (no Java installation required on host)
3. **EMS Swing Application** (with all 15 subjects & 750 pre-seeded questions)
4. **Web GUI Gateway** (streamed via HTML5 / noVNC to any browser on port **8080**)

---

## 1. Quick Start (Local or Server)

### Step 1: Clone and Start Container
Run one command in the project directory:

```bash
docker compose up -d --build
```

### Step 2: Open in Any Web Browser
Visit:
```text
http://localhost:8080
```
*(Or `http://<your-server-ip>:8080` if running on a remote cloud server).*

The complete Exam Management System desktop window will appear inside your browser window. No software, Java, or database setup is needed on the client machine!

---

## 2. Default Login Credentials

### Administrator
- **Username**: `superadmin`
- **Password**: `123456`
- **Role**: `ADMIN` (Dashboard, Question Bank, Scheduling, Analytics)

### Sample Students
All 20 pre-seeded students use password `Password@123`:
- `aaravs` / `Password@123`
- `ananyav` / `Password@123`
- `rohanp` / `Password@123`

---

## 3. How to Deploy Live to the Cloud

You can run this container on any cloud virtual machine or container host:

### Option A: Cloud VM (DigitalOcean, AWS EC2, Hetzner, Linode)
1. Launch an Ubuntu Linux server ($4–$6/month).
2. Install Docker:
   ```bash
   curl -fsSL https://get.docker.com | sh
   ```
3. Clone your GitHub repository:
   ```bash
   git clone https://github.com/praveshgarg099/exam-management-system.git
   cd exam-management-system
   ```
4. Start the container in background:
   ```bash
   docker compose up -d --build
   ```
5. Open `http://YOUR-SERVER-IP:8080` in your browser. It is now live for anyone with the link!

### Option B: Add a Custom Domain with Free HTTPS (Caddy / Nginx)
To give users a clean link like `https://exam.yourdomain.com`:
Point your domain's DNS `A` record to your server IP, install [Caddy](https://caddyserver.com/), and add to `/etc/caddy/Caddyfile`:
```caddy
exam.yourdomain.com {
    reverse_proxy localhost:8080
}
```
Caddy automatically provisions free SSL certificates. Users now take exams at `https://exam.yourdomain.com` securely!

---

## 4. Useful Management Commands

### View live logs
```bash
docker compose logs -f
```

### Stop the container
```bash
docker compose down
```

### Restart the container
```bash
docker compose restart
```

### Backup database data
All database tables and test results are stored in Docker volume `ems-database-volume`. Data is automatically preserved even if you stop, rebuild, or update the container.
