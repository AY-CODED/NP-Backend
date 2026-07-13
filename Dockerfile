<<<<<<< HEAD
# Use Java 17
FROM eclipse-temurin:17-jdk

# Working directory
WORKDIR /app

# Copy jar file
COPY target/NpApplication-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Start application
=======
# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

>>>>>>> a64ac9a2035039bc08899fdbaf867e95c6608ba9
ENTRYPOINT ["java","-jar","app.jar"]