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

### ☁️ 連線新的雲端資料庫（GCP Cloud SQL - MySQL）
此專案已加入透過 Cloud SQL Proxy 連到雲端 MySQL 的流程。Proxy 會在本機開 `127.0.0.1:3307`，與 `src/main/resources/application.yml` 設定一致。

1) 啟動 Cloud SQL Proxy（請保持視窗開啟）
```powershell
.\cloud-sql-proxy.exe <GCP_PROJECT>:<REGION>:<INSTANCE_NAME> --credentials-file key.json --port 3307
```
- `key.json`：請使用具備「Cloud SQL Client」權限的服務帳戶金鑰，勿提交到版本庫。
- `INSTANCE_NAME`：GCP Console 上的 Instance connection name。

2) 驗證連線（使用你在雲端 DB 上的使用者/密碼）
```powershell
mysql -h 127.0.0.1 -P 3307 -u <DB_USER> -p
# 進入後
SHOW DATABASES;
```
- 預期能看到你的資料庫（例如：`zapp_demo_db`）。

3) 啟動 Spring Boot（會連到 Proxy）
```powershell
.\mvnw.cmd spring-boot:run
```

如需以環境變數覆蓋 `application.yml`（可選）：
```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3307/zapp_demo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:SPRING_DATASOURCE_USERNAME="<DB_USER>"
$env:SPRING_DATASOURCE_PASSWORD="<DB_PASSWORD>"
```
```bash
# macOS / Linux / WSL（可選）
export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3307/zapp_demo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export SPRING_DATASOURCE_USERNAME=<DB_USER>
export SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD>
```

桌面工具連線（DBeaver / DataGrip）
- Host: `127.0.0.1`
- Port: `3307`
- Database: 你的雲端 DB 名稱（例：`zapp_demo_db`）
- User/Password: 你的雲端 DB 帳密
- SSL: 不需額外設定（由 Proxy 處理）

安全性建議
- 勿在版本庫內保留任何帳密；`key.json` 僅在本機保存。
- 僅以 Proxy 連線生產 DB；限制來源 IP 或走私有路徑。

---

```bash
# macOS / Linux / WSL 直接啟動（若環境變數已配置或沿用 application.yml）
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

Note on npm usage
- 請在 `frontend/` 目錄執行所有 npm 指令（如 `npm install`, `npm run dev`, `npm run build`）。
- 專案根目錄不再保留 Node 專案設定（已移除根目錄的 `package.json` 與 `package-lock.json`），避免混淆與誤裝依賴。

## 🧪 Verify Integration

1. Run both backend and frontend.
2. Open [http://localhost:5173](http://localhost:5173).
3. Register or login from the UI.
4. Or test via curl:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login      -H "Content-Type: application/json"      -d '{"username":"demo","password":"demo"}'
   ```

---

## 🔎 DB Health Check

你可以用預先準備好的健康檢查腳本快速盤點目前資料庫狀況：

1) 確認 Cloud SQL Proxy 已啟動在 `127.0.0.1:3307`。

2) 執行腳本（會互動要求密碼）：
```powershell
mysql -h 127.0.0.1 -P 3307 -u <DB_USER> -p < scripts/mysql-health.sql
```

會輸出：
- 版本與時間
- Uptime、連線數、Threads 使用情況
- 各資料庫與前 20 大表大小（MB）
- InnoDB Buffer Pool 命中率
- 鎖等待（若有）
- Top 慢/重查詢摘要（需 performance_schema）
- InnoDB Engine 狀態摘要

腳本位置：`scripts/mysql-health.sql`

---

## 📊 Data Inspect（查看目前 DB 內容）

快速列出目前 schema 的資料表、大小與精準筆數：

```powershell
# 建議直接指定 schema 連線（zapp_demo_db 請替換為你的 DB 名稱）
mysqlsh --sql root@127.0.0.1:3307/zapp_demo_db -p -f scripts/mysql-introspect.sql
```

說明：
- 輸出表清單、近似列數、大小（MB），並逐表進行 COUNT(*) 取得精準筆數（大表會較花時間）。
- 只想看某一表內容，可在互動模式輸入：
  - `USE zapp_demo_db;`
  - `SHOW TABLES;`
  - `SELECT * FROM users LIMIT 50;`（將 `users` 換成你的表名）

腳本位置：`scripts/mysql-introspect.sql`

---

小抄：直接查看使用者清單

```powershell
mysqlsh --sql root@127.0.0.1:3307/zapp_demo_db -p -f scripts/users-preview.sql
```

或進入互動模式後執行：
```
USE zapp_demo_db;
SELECT COUNT(*) FROM users;
SELECT id, username, email, password, roles, enabled FROM users ORDER BY id DESC LIMIT 100;
```

腳本位置：`scripts/users-preview.sql`

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
## 部署與資料庫連線（Cloud SQL Java Connector）

本專案後端預設採用 Cloud SQL Java Connector 連線至 GCP Cloud SQL（MySQL）。相較 Cloud SQL Proxy，Connector 不需額外行程與本機埠，維運較簡單，適合未來綁定自有網域的長期部署。

### 必要環境變數（建議以服務器環境或 Secret Manager 注入）
- `CLOUD_SQL_INSTANCE`：Cloud SQL 連線名稱（`<PROJECT>:<REGION>:<INSTANCE>`）
- `DB_NAME`：資料庫名稱（預設 `zapp_demo_db`）
- `SPRING_DATASOURCE_USERNAME`：DB 使用者（建議非 root）
- `SPRING_DATASOURCE_PASSWORD`：DB 密碼
- `JWT_SECRET`：JWT 簽章密鑰（至少 64 字元高熵字串）
- `CORS_ALLOWED_ORIGINS`：允許的前端網域，逗號分隔（預設 `http://localhost:5173`）

Spring Boot 會用以上變數組合出 Connector URL：
`jdbc:mysql:///${DB_NAME}?cloudSqlInstance=${CLOUD_SQL_INSTANCE}&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false`
若想自行提供完整 URL，可直接設定 `SPRING_DATASOURCE_URL` 覆蓋。

### Maven 依賴
已在 `pom.xml` 加入：
- `com.google.cloud.sql:mysql-socket-factory-connector-j-8`
- `com.mysql:mysql-connector-j`

### 啟動示例（PowerShell）
```powershell
$env:CLOUD_SQL_INSTANCE = "<PROJECT>:<REGION>:<INSTANCE>"
$env:DB_NAME = "zapp_demo_db"
$env:SPRING_DATASOURCE_USERNAME = "app_user"
$env:SPRING_DATASOURCE_PASSWORD = "<你的密碼>"
$env:JWT_SECRET = "<至少64字元的隨機字串>"
$env:CORS_ALLOWED_ORIGINS = "https://你的前端網域,https://www.你的前端網域"
.
\mvnw.cmd spring-boot:run
```

### 本機開發（可選）
- 若你偏好 Cloud SQL Proxy，也可自行啟動 Proxy，然後以 `SPRING_DATASOURCE_URL` 覆蓋成 `jdbc:mysql://127.0.0.1:3307/<DB_NAME>?useSSL=false`。
- 建議仍以 Connector 為主，以縮減維運元件。

### 安全注意事項
- 機密不入庫：不要把密碼、JWT 秘鑰或 `key.json` 放到版控。
- CORS 請設定為你的實際前端網域。
- 正式環境建議關閉 `show-sql` 與以 migration 管理 schema（`ddl-auto` 適合開發環境）。

## Windows PowerShell Execution Policy
若在 PowerShell 執行 `npm` 或專案腳本時出現 `npm.ps1 cannot be loaded`，請設定：
```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```
或使用專案腳本繞過：
```powershell
powershell -ExecutionPolicy Bypass -File .\frontend\run-dev.ps1
# 指定埠範例
powershell -ExecutionPolicy Bypass -File .\frontend\run-dev.ps1 -- --port 5174
```
