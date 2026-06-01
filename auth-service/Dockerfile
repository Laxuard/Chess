# ==========================================
# BUILD STAGE
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache Maven dependency layers for rapid rebuilds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# RUN STAGE
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for robust healthcheck support
RUN apk add --no-cache curl

# Copy the built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
