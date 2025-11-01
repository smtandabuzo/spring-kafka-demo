# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Copy SSL certificates
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install required tools for SSL
RUN apk --no-cache add ca-certificates && \
    mkdir -p /app/certs

# Copy the JAR file and certificates
COPY --from=builder /app/target/*.jar app.jar
#COPY --from=builder /app/certs/ /app/certs/

# Create non-root user and set permissions
RUN addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
