# 1️⃣ Use a base image with Java 21
FROM eclipse-temurin:21-jdk-jammy

# 2️⃣ Set working directory
WORKDIR /app

# 3️⃣ Copy the JAR file (built by Maven)
COPY target/*.jar app.jar

# 4️⃣ Expose default Spring Boot port
EXPOSE 8082

# 5️⃣ Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]