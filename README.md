# Demo Auth – 後端最新執行/部署說明（更新版）

本文件為目前專案的最新、實用執行與部署指南（後端優先）。早期舊內容已移除以避免混淆。

## 快速總覽
- 後端：Java 17 + Spring Boot 3.5（內建 Tomcat）
- DB 連線：Cloud SQL Java Connector（正式環境推薦）；本機亦可改走 Cloud SQL Proxy（127.0.0.1:3307）
- 機密不入庫：DB 密碼與 JWT 秘鑰以環境變數提供
- CORS 可用環境變數設定允許的前端網域

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

