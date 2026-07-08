# Stage 1: Giai đoạn biên dịch (Builder stage)
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Sao chép toàn bộ mã nguồn vào container
# Kết hợp .dockerignore để loại bỏ target/, .mvn/wrapper cache nếu cần
COPY . .

# Cấp quyền thực thi và biên dịch JAR bằng Maven Wrapper
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

# Stage 2: Giai đoạn chạy ứng dụng (Runtime stage)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Sao chép file JAR đã biên dịch từ Stage 1 sang Stage 2
COPY --from=builder /app/target/*.jar app.jar

# Khai báo cổng mạng ứng dụng và lệnh khởi chạy
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]