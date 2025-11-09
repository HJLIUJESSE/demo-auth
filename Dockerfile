## Multi-stage build; 不影響本地執行，只供需要時使用

# 1) Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -q -DskipTests clean package && \
    mkdir -p out && \
    cp target/*.jar out/app.jar

# 2) Runtime stage
FROM eclipse-temurin:17-jre
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/out/app.jar /app/app.jar

# Cloud Run 等平台會注入 PORT；本地可改用 -p 8080:8080
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]

