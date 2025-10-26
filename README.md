# Demo Auth 環境建置指南

本專案提供 Spring Boot 3.5 後端與 React 19 + Vite 前端的身分驗證範例。以下指引說明如何在本機架設 MySQL、啟動後端服務，並配置前端開發環境。

## 系統需求
- Java 17（建議採用 Eclipse Temurin 或 Amazon Corretto）
- Maven Wrapper（已隨倉庫提供 `mvnw`，無須額外安裝 Maven）
- Node.js 20 LTS 與 npm（可透過 nvm 安裝與切換版本）
- MySQL 8.0 以上
- Git 2.40 以上
- （選用）Docker 與 Docker Compose，若偏好容器化部署 MySQL

## 取得專案原始碼
```bash
git clone https://github.com/<your-org>/demo-auth.git
cd demo-auth
```

## 設定 MySQL
1. 啟動 MySQL 服務後，建立資料庫與帳號：
   ```sql
   CREATE DATABASE demo_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'demo_user'@'localhost' IDENTIFIED BY 'StrongPassword123!';
   GRANT ALL PRIVILEGES ON demo_auth.* TO 'demo_user'@'localhost';
   FLUSH PRIVILEGES;
   ```
2. 若採用 Docker，可使用以下 `docker run` 範例快速啟動：
   ```bash
docker run --name demo-auth-mysql -e MYSQL_ROOT_PASSWORD=changeme \
  -e MYSQL_DATABASE=demo_auth -e MYSQL_USER=demo_user \
  -e MYSQL_PASSWORD=StrongPassword123! -p 3306:3306 -d mysql:8.0
```
3. 調整 `src/main/resources/application.yml`：
   - 更新 `spring.datasource.username/password` 以符合實際帳密。
   - 將 `jwt.secret` 改成至少 64 字元的隨機字串，可透過 `openssl rand -base64 64` 產生。
   - 生產環境建議改用 `ddl-auto: validate` 並透過 Flyway 或 Liquibase 管理 schema。
4. 亦可改用環境變數覆寫設定，例如：
   ```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/demo_auth?useSSL=false&serverTimezone=Asia/Taipei"
export SPRING_DATASOURCE_USERNAME=demo_user
export SPRING_DATASOURCE_PASSWORD=StrongPassword123!
export JWT_SECRET=$(openssl rand -base64 64)
```

## 啟動 Spring Boot 後端
```bash
./mvnw spring-boot:run
```
- 首次執行會下載依賴，完成後可透過 `http://localhost:8080/api/auth/register` 與 `http://localhost:8080/api/auth/login` 測試 API。
- 若需打包成 JAR，可執行 `./mvnw clean package`，輸出檔位於 `target/demo-auth-0.0.1-SNAPSHOT.jar`。
- 執行測試套件：`./mvnw test`。

## 設定 React + Vite 前端
```bash
cd frontend
npm install
npm run dev
```
- 預設開發伺服器為 `http://localhost:5173`，並將請求轉發至 `http://localhost:8080`。
- 建議在提交前執行 `npm run lint` 確保程式碼符合 ESLint 規範。
- 若需產出靜態檔案供部署，執行 `npm run build` 生成 `frontend/dist`。

## 驗證安裝
1. 啟動後端與前端後，透過瀏覽器開啟 `http://localhost:5173` 進行註冊、登入流程。
2. 使用 Postman 或 `curl` 測試 API，例如：
   ```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo"}'
```
3. 若需要紀錄 OAuth/JWT 憑證，請勿將秘密金鑰或憑證提交到版本控制，改以環境變數或密鑰管理服務保存。

## 進一步部署
- 若要部署至雲端，請改用受管理的 MySQL（如 AWS RDS），並確保防火牆僅允許應用程式存取。
- 建議搭配 CI/CD（GitHub Actions、GitLab CI 等）於合併前自動執行 `./mvnw test` 與 `npm run lint`。
- 生產環境請將 `allowedOrigins` 限縮為受信任網域，並於反向代理層設定 HTTPS。
