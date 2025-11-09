# Demo Auth – 後端最新執行/部署說明（更新版）

本文件為目前專案的最新、實用執行與部署指南（後端優先）。早期舊內容已移除以避免混淆。

## 快速總覽
- 後端：Java 17 + Spring Boot 3.5（內建 Tomcat）
- DB 連線：Cloud SQL Java Connector（正式環境推薦）；本機亦可改走 Cloud SQL Proxy（127.0.0.1:3307）
- 機密不入庫：DB 密碼與 JWT 秘鑰以環境變數提供
- CORS 可用環境變數設定允許的前端網域

## 最近更新（2025-11-10）
- 新增忘記密碼流程：
  - `POST /api/auth/forgot-password`（產生一次性重設 token，dev 寄信記 log、prod 寄信）；
  - `POST /api/auth/reset-password`（帶 token 與新密碼重設）。
- 郵件寄送：
  - Dev：`NoopMailer`（不寄信，log 顯示重設連結，支援 sandbox/白名單）
  - Prod：`SmtpMailer`（支援 Gmail/SendGrid/SES 等 SMTP；品牌化 HTML 模板；關閉 click/open 追蹤）
  - 新增 sandbox（`APP_MAIL_SANDBOX_ENABLED`）與白名單（`APP_MAIL_ALLOWLIST`）
- 速率限制（忘記密碼）：IP 與 Email 雙重限制（預設 15 分鐘 5 次），超限一律 200 但不產生 token。
- 個人頁 API 與前端：
  - `GET/PUT /api/profile`（生日）與 `POST /api/profile/avatar`（頭像上傳）
  - 靜態檔對外：`/uploads/**`
  - 前端新增 `ForgotPassword`、`ResetPassword`、`Dashboard`（含個人頁）
- 其他：新增 Actuator `/actuator/health`、`Dockerfile` 多階段建置。

## 必要環境變數
- `CLOUD_SQL_INSTANCE`：Cloud SQL 連線名稱（格式：`<PROJECT>:<REGION>:<INSTANCE>`）
- `DB_NAME`：資料庫名稱（預設 `zapp_demo_db`）
- `SPRING_DATASOURCE_USERNAME`：DB 使用者（建議非 root 的應用帳號）
- `SPRING_DATASOURCE_PASSWORD`：DB 密碼
- `JWT_SECRET`：JWT 簽章密鑰（至少 64 字元高熵字串，過短會導致啟動失敗）
- `CORS_ALLOWED_ORIGINS`：允許的前端網域（逗號分隔；本機預設 `http://localhost:5173`）

> 可選覆蓋：`SPRING_DATASOURCE_URL`（若指定，會直接使用此 JDBC URL，常用於本機 Proxy）

## 本機執行（方案 A：Cloud SQL Java Connector，推薦）
1) 提供 ADC（擇一）
   - 使用服務帳戶金鑰：設定 `GOOGLE_APPLICATION_CREDENTIALS` 指向 `key.json`
   - 或使用 gcloud：`gcloud auth application-default login`
2) 設定環境變數（PowerShell 範例）
```powershell
$env:CLOUD_SQL_INSTANCE = "<PROJECT>:<REGION>:<INSTANCE>"
$env:DB_NAME = "zapp_demo_db"
$env:SPRING_DATASOURCE_USERNAME = "app_user"
$env:SPRING_DATASOURCE_PASSWORD = "<你的密碼>"
$env:JWT_SECRET = "<至少64字元>"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"
```
3) 啟動
```powershell
.\mvnw.cmd spring-boot:run
```

## 本機執行（方案 B：Cloud SQL Proxy，替代）
1) 先啟 Proxy（示例）
```powershell
.\cloud-sql-proxy.exe <PROJECT:REGION:INSTANCE> --credentials-file "D:\secrets\cloudsql\key.json" --address 127.0.0.1 --port 3307 --verbose
```
2) 覆蓋為 Proxy 連線（務必單行無換行）
```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:mysql://127.0.0.1:3307/zapp_demo_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true'
$env:SPRING_DATASOURCE_USERNAME = 'app_user'
$env:SPRING_DATASOURCE_PASSWORD = '<你的密碼>'
$env:JWT_SECRET = '<至少64字元>'
```
3) 啟動
```powershell
.\mvnw.cmd spring-boot:run
```

## 埠號與 CORS
- 預設綁定 `8080`；臨時改埠可用：`-Dserver.port=8081` 或 `SERVER_PORT=8081`
- CORS 以 `CORS_ALLOWED_ORIGINS` 設定（後端由 `app.cors.allowed-origins` 讀取）

## 前端（Vite + React）
- 僅在 `frontend/` 目錄執行 npm 指令
- 建議使用腳本（繞過 PowerShell 限制）：
```powershell
powershell -ExecutionPolicy Bypass -File .\frontend\run-dev.ps1
# 改埠範例
powershell -ExecutionPolicy Bypass -File .\frontend\run-dev.ps1 -- --port 5174
```
- 若出現 `npm.ps1 cannot be loaded`，可先執行：
```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```
- API 走 `/api`，由 Vite 代理到後端 `http://localhost:8080`（見 `frontend/vite.config.ts`）

## 部署建議
### Cloud Run（推薦）
- 優點：免維運 VM、內建 HTTPS、可綁自訂網域、整合 Cloud Logging/Monitoring/Trace
- 作法摘要：
  1. 建立 Dockerfile（將可執行 JAR 打包；`ENTRYPOINT ["java","-Dserver.port=${PORT}","-jar","/app/app.jar"]`）
  2. `gcloud builds submit --tag gcr.io/<PROJECT>/demo-auth:latest`
  3. `gcloud run deploy demo-auth --image gcr.io/<PROJECT>/demo-auth:latest --region <REGION> --allow-unauthenticated --service-account <SA>@<PROJECT>.iam.gserviceaccount.com --add-cloudsql-instances <PROJECT:REGION:INSTANCE> --set-env-vars CLOUD_SQL_INSTANCE=...,DB_NAME=...,SPRING_DATASOURCE_USERNAME=...,SPRING_DATASOURCE_PASSWORD=...,JWT_SECRET=...,CORS_ALLOWED_ORIGINS=https://你的網域`
  4. 在 Cloud Run Console 綁定自訂網域（HTTPS 自動）

### GCE VM（Compute Engine）
- 作法摘要：
  1. 建 VM 並附掛服務帳戶（含 `roles/cloudsql.client`），安裝 Java 17
  2. 將 JAR 放至 `/opt/demo-auth/demo-auth.jar`，建立 `/etc/demo-auth/env`（放前述環境變數）
  3. 建立 systemd 服務啟動後端；Nginx 反代到 8080 並用 Certbot 憑證

## 安全重點
- 機密不入庫：DB 密碼、JWT 秘鑰、服務金鑰不要提交到 Git
- JWT 秘鑰足夠長（64+ 字元），定期輪替
- 正式關閉 `show-sql`，以 Migration 工具（如 Flyway）管理 schema
- CORS 僅允許你的正式網域

## 附註
- 後端設定檔：`src/main/resources/application.yml`
- CORS 設定載入處：`src/main/java/com/example/demo_auth/WebConfig.java`
- Cloud SQL Connector 依賴已加入於 `pom.xml`

## SMTP/忘記密碼（prod）
- 啟用方式：設定 `SPRING_PROFILES_ACTIVE=prod` 與 SMTP 參數（以下以 Gmail 為例）
```
APP_MAIL_FROM=your@gmail.com
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your@gmail.com
SPRING_MAIL_PASSWORD=<Gmail 應用程式密碼>
SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH=true
SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE=true
```
- dev 預設不寄信（只回 token 與記錄 log）；prod 預設寄信且不回 token。
- 重設連結前綴：`APP_RESET_BASE_URL`（預設為 `http://localhost:5173/reset-password?token=`）。

### 郵件 Sandbox 與白名單（上線規格）
- 設定：
  - `APP_MAIL_SANDBOX_ENABLED=true|false`：開啟後，非白名單收件人將被改寄到 `APP_MAIL_SANDBOX_RECIPIENT`
  - `APP_MAIL_SANDBOX_RECIPIENT`：沙盒收件者（例如 `qa-inbox@example.com`）
  - `APP_MAIL_ALLOWLIST`：以逗號分隔的允許清單，支援完整 email 或網域（如 `example.com,staff@company.com`）
  - `APP_MAIL_FROM_NAME`：寄件顯示名稱（預設 `Demo App`）
- 預設：`application-dev.yml` 啟用 sandbox；`application-prod.yml` 關閉 sandbox。

### 速率限制（忘記密碼）
- 參數：
  - `APP_RATE_FP_MAX`（預設 5）：每視窗最大請求數（按 IP 與 Email 各計）
  - `APP_RATE_FP_WINDOW`（預設 900 秒）：視窗秒數
- 超限時 API 仍回 200，但不產生 token（不洩漏資訊）。

## 前端（重設密碼與個人頁）
- 新增頁面與功能：
  - `ForgotPassword`、`ResetPassword`（可從登入頁點「Forgot password?」或直接開 `/reset-password?token=`）
  - 登入後 `Dashboard`：三個頁籤（首頁、群聊、個人）；個人頁可編輯生日與上傳頭像（對應 `/api/profile` 與 `/api/profile/avatar`）。

## 忘記密碼（功能與環境設定總表）
- 端點
  - `POST /api/auth/forgot-password`：輸入 email → 建立重設 token → dev 記錄重設連結、prod 寄信；回應一律 200。
  - `POST /api/auth/reset-password`：輸入 token + newPassword → 成功 200。
- 寄信設定（必要）
  - `APP_MAIL_ENABLED`：true 開啟寄信；false 僅記 log（dev 預設）。
  - `APP_MAIL_FROM`：寄件地址（Single Sender 用經驗證的單一 Email；正式用網域如 `no-reply@yourdomain.com`）。
  - `APP_MAIL_FROM_NAME`：寄件顯示名稱（品牌名）。
  - `SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD`：SMTP 參數（SendGrid：host `smtp.sendgrid.net`、port `587`、username `apikey`、password 為 API Key）。
  - `APP_RESET_BASE_URL`：重設連結前綴（dev：`http://localhost:5173/reset-password?token=`，prod 請改正式 HTTPS 網域）。
- 寄信補充（可選）
  - `APP_MAIL_REPLY_TO`：使用者回覆信件的地址。
  - `APP_MAIL_SUPPORT_URL`：信內的「客服/支援」連結。
  - `APP_MAIL_SANDBOX_ENABLED`：開啟 sandbox 時，非白名單收件者改寄到 sandbox 收件箱。
  - `APP_MAIL_SANDBOX_RECIPIENT`：sandbox 收件箱地址（例如 qa-inbox@company.com）。
  - `APP_MAIL_ALLOWLIST`：白名單（逗號分隔；支援完整 email 或網域，如 `company.com,staff@partner.com`）。
  - 交易信已預設關閉 click/open 追蹤並加上分類 `password_reset`。
- 速率限制（預設值，環境變數可覆寫）
  - `APP_RATE_FP_MAX`：每視窗最大次數（預設 5）
  - `APP_RATE_FP_WINDOW`：視窗秒數（預設 900）
- Profile 切換
  - `SPRING_PROFILES_ACTIVE=dev|prod`；dev 會使用 `NoopMailer`（不寄信，log 顯示重設連結）；prod 啟用 SMTP 寄信。

## 開發測試指南（SendGrid Single Sender 快速上手）
1) SendGrid 後台建立 Single Sender（From Email = 你的收信信箱）並完成驗證；建立 API Key。
2) PowerShell 設定（同視窗）：
```
$env:APP_MAIL_ENABLED='true'
$env:APP_MAIL_SANDBOX_ENABLED='false'
$env:APP_MAIL_FROM='<你的Single Sender Email>'
$env:APP_MAIL_FROM_NAME='Demo App'
$env:SPRING_MAIL_HOST='smtp.sendgrid.net'
$env:SPRING_MAIL_PORT='587'
$env:SPRING_MAIL_USERNAME='apikey'
$env:SPRING_MAIL_PASSWORD='<你的SendGrid API Key>'
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH='true'
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE='true'
$env:APP_RESET_BASE_URL='http://localhost:5173/reset-password?token='
$env:JWT_SECRET='<64+隨機字元>'
# H2 檔案模式（重啟不清空）
$env:SPRING_DATASOURCE_URL='jdbc:h2:file:./localdb/demo;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false'
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME='org.h2.Driver'
$env:SPRING_DATASOURCE_USERNAME='sa'
$env:SPRING_DATASOURCE_PASSWORD=''
```
3) 啟動後端 `.\mvnw.cmd spring-boot:run`，前端 `npm run dev`。
4) 先註冊該 Email → 回登入頁按 Forgot password → 到收件匣收信 → 點連結到 `/reset-password?token=...`。

## 正式上線建議（提升投遞率）
- 在 SendGrid 完成 Domain Authentication（SPF/DKIM/Link Branding/Return‑Path）並改用 `no-reply@你的網域` 作為 From。
- DNS 加上 DMARC（先 `p=none` 觀察，再逐步 `quarantine/reject`）。
- `APP_RESET_BASE_URL` 改指向正式 HTTPS 網域，避免 `localhost` 連結降評。
- 維持低退信/投訴率、名單衛生、並在 Gmail Postmaster Tools 監控網域信譽。

## 路線圖（Roadmap）
- 郵件/寄信
  - SendGrid Web API 整合（取代 SMTP）、模板化與多語系主旨。
  - 完成正式網域認證與 DMARC 導入；依環境自動關閉/開啟追蹤。
- 後端
  - Flyway 導入與 `ddl-auto=validate`（prod）。
  - 密碼重設成功後註銷既有登入態/refresh token。
  - 擴充登入速率限制/登入失敗鎖定、審計日誌與結構化 JSON log/Request‑ID。
- 前端
  - 忘記密碼/重設頁品牌化樣式與可用性強化；簡易路由與錯誤提示。
  - 個人頁更多欄位（暱稱、性別、地區）與驗證、檔案大小/格式限制。
- 活動功能
  - 報名：候補機制與名額釋放、匯出、欄位遮罩。
  - 群聊：WebSocket/SSE、訊息稽核與濫用控制。
  - 地點：地理編碼快取與第三方地圖供應商切換。
