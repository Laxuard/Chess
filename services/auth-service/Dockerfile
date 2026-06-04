# ==========================================
# BUILD STAGE
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy the shared common-core library first and install it
COPY services/common-core ./services/common-core
RUN mvn -f services/common-core/pom.xml clean install -DskipTests --no-transfer-progress

# Copy auth-service sources and build it
COPY services/auth-service ./services/auth-service
RUN mvn -f services/auth-service/pom.xml clean package -DskipTests --no-transfer-progress

# ==========================================
# RUN STAGE
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for robust healthcheck support
RUN apk add --no-cache curl

# Copy the built jar from the builder stage
COPY --from=builder /app/services/auth-service/target/*.jar app.jar

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
