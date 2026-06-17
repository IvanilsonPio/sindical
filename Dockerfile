# Multi-stage build for Spring Boot application

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Create runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user and directories with correct ownership
RUN apk add --no-cache curl \
    && addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/uploads/arquivos-gerais /app/logs \
    && chown -R spring:spring /app

USER spring:spring

# Copy the built JAR from build stage
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar
COPY --chown=spring:spring docker-entrypoint.sh docker-entrypoint.sh
RUN chmod +x docker-entrypoint.sh

# Expose application port
EXPOSE 8080

# Health check - start-period allows time for JVM and Spring to fully start
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["./docker-entrypoint.sh"]
