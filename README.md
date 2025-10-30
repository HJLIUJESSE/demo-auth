# 🧩 Demo Auth Setup Guide (Windows + WSL Ready)

This repository provides a **Spring Boot 3.5 backend** and a **React 19 + Vite frontend** implementing a secure authentication flow.  
Follow this guide to build and run the stack correctly on **Windows or WSL**.

---

## 🧱 Prerequisites

| Tool | Version | Install Method |
|------|----------|----------------|
| **Java** | 17 | Temurin / Corretto |
| **Maven Wrapper** | (bundled) | no need to install globally |
| **Node.js** | 20 LTS | via nvm / winget |
| **MySQL** | 8.0+ | via winget / Docker |
| **Git** | 2.40+ | via winget or git-scm |
| **Optional:** | Docker | for containerized MySQL |

---

## ⚙️ Environment Setup

### 🧠 1. Java 17
```bash
# Windows (PowerShell as Admin)
winget install EclipseAdoptium.Temurin.17.JDK

# macOS
brew install --cask temurin17

# Ubuntu / Debian
sudo apt update && sudo apt install openjdk-17-jdk
```
Verify installation:
```bash
java -version
```

---

### 🧩 2. Maven Wrapper

Maven Wrapper (`mvnw` / `mvnw.cmd`) is included in the repository.

```bash
# macOS / Linux / WSL
chmod +x mvnw
./mvnw -v

# Windows PowerShell
.\mvnw.cmd -v
```

If you encounter permission issues on macOS or WSL:
```bash
git config core.fileMode false
```

---

### ⚡ 3. Node.js 20 LTS + npm

- **Using nvm (recommended):**
  ```bash
  nvm install 20
  nvm use 20
  ```
- **Windows (PowerShell admin):**
  ```bash
  winget install OpenJS.NodeJS.LTS
  ```
- **macOS:**
  ```bash
  brew install node@20
  ```
- **Ubuntu/Debian:**
  ```bash
  curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
  sudo apt install nodejs
  ```

Verify versions:
```bash
node -v
npm -v
```

⚠️ **Fix for npm.ps1 PowerShell security error:**
```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

---

### 🐬 4. MySQL 8.0 Installation

#### 🅰️ Option A — Winget (Windows Native)
```powershell
winget install Oracle.MySQL
```

After installation:
```powershell
# Check service
sc query state= all | findstr /I "mysql"

# Start MySQL service (name may differ, e.g. MySQL84)
net start MySQL84
```

Connect to MySQL:
```powershell
cd "C:\Program Files\MySQL\MySQL Server 8.4\bin"
.\mysql -u root -p
```

If no password was set during installation:
1. Locate temporary password (optional):  
   Check `%PROGRAMDATA%\MySQL\MySQL Server 8.4\Data\*.err`
2. Set new root password:
   ```sql
   ALTER USER 'root'@'localhost' IDENTIFIED BY 'StrongRoot123!';
   FLUSH PRIVILEGES;
   ```

#### 🅱️ Option B — Docker
```bash
docker run --name demo-auth-mysql   -e MYSQL_ROOT_PASSWORD=StrongRoot123!   -e MYSQL_DATABASE=demo_auth   -e MYSQL_USER=demo_user   -e MYSQL_PASSWORD=StrongPassword123!   -p 3306:3306 -d mysql:8.0
```

---

### 🧰 5. Database Setup
Login and create database:
```sql
CREATE DATABASE demo_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'demo_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
GRANT ALL PRIVILEGES ON demo_auth.* TO 'demo_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 🌍 Environment Variables

### 🪟 PowerShell (Windows)
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/demo_auth?useSSL=false&serverTimezone=Asia/Taipei"
$env:SPRING_DATASOURCE_USERNAME="demo_user"
$env:SPRING_DATASOURCE_PASSWORD="StrongPassword123!"
$env:JWT_SECRET=[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Max 256 }))
```

### 🐧 WSL / macOS / Linux
```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/demo_auth?useSSL=false&serverTimezone=Asia/Taipei"
export SPRING_DATASOURCE_USERNAME=demo_user
export SPRING_DATASOURCE_PASSWORD=StrongPassword123!
export JWT_SECRET=$(openssl rand -base64 64)
```

---

## 🚀 Run the Backend (Spring Boot)
```bash
./mvnw spring-boot:run
```
Expected output:
```
Tomcat started on port(s): 8080
Started DemoAuthApplication in 5.432 seconds
```

Useful commands:
```bash
./mvnw clean package   # build jar to target/
./mvnw test            # run backend test suite
```

---

## 💻 Run the Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
```

Then visit:
👉 **http://localhost:5173**

The frontend automatically proxies API calls to `localhost:8080`.

---

## 🧪 Verify Integration

1. Run both backend and frontend.
2. Open [http://localhost:5173](http://localhost:5173).
3. Register or login from the UI.
4. Or test via curl:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login      -H "Content-Type: application/json"      -d '{"username":"demo","password":"demo"}'
   ```

---

## 🛠️ Troubleshooting

| Problem | Solution |
|----------|-----------|
| `Access denied for user 'root'@'localhost'` | Reset root password and regrant privileges |
| `Can't connect to MySQL server (10061)` | Start MySQL service: `net start MySQL84` |
| `npm.ps1 cannot be loaded` | Run `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` |
| `Port 8080 already in use` | Modify `server.port` in `application.yml` |
| Node version mismatch | Run `nvm install 20 && nvm use 20` and reinstall deps |
| `mvnw permission denied` | Run `chmod +x mvnw` (for WSL/Linux/macOS) |

---

## 🚢 Deployment Tips

- Store secrets and DB credentials in environment variables or vaults.
- For production, set `spring.jpa.hibernate.ddl-auto=validate`.
- Enable HTTPS and restrict CORS origins.
- Run CI steps: `./mvnw test` and `npm run lint` before merging.

---

## 📜 Optional One-Click Setup Script

If you prefer, you can automate MySQL + env setup on Windows PowerShell:
```powershell
cd "C:\Program Files\MySQL\MySQL Server 8.4\bin"
.\mysql -u root -pStrongRoot123! -e "CREATE DATABASE demo_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
.\mysql -u root -pStrongRoot123! -e "CREATE USER 'demo_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';"
.\mysql -u root -pStrongRoot123! -e "GRANT ALL PRIVILEGES ON demo_auth.* TO 'demo_user'@'localhost'; FLUSH PRIVILEGES;"
```

Then:
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/demo_auth?useSSL=false&serverTimezone=Asia/Taipei"
$env:SPRING_DATASOURCE_USERNAME="demo_user"
$env:SPRING_DATASOURCE_PASSWORD="StrongPassword123!"
$env:JWT_SECRET=[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Max 256 }))
```

Finally:
```powershell
.\mvnw.cmd spring-boot:run
cd frontend
npm install
npm run dev
```

---

✅ **You’re ready!**
Access your app at:
- Backend → http://localhost:8080  
- Frontend → http://localhost:5173
