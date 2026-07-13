# Use Java 17
FROM eclipse-temurin:17-jdk

# Working directory
WORKDIR /app

# Copy jar file
COPY target/NpApplication-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Start application
ENTRYPOINT ["java","-jar","app.jar"]