# Chọn base image
FROM eclipse-temurin:17-jdk-alpine

# Thư mục làm việc trong container
WORKDIR /app

# Copy file JAR vào container
COPY target/*.jar app.jar

# Expose cổng
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]