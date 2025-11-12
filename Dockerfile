# Multi-stage Dockerfile: build outside or rely on prebuilt artifact
# Stage 0: optional builder (commented out to prefer local build caching)
# FROM maven:3.8.8-eclipse-temurin-17 AS builder
# WORKDIR /workspace
# COPY . /workspace
# RUN mvn -B -Puberjar -DskipTests clean package

# Stage 1: runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/java-load-balancer-parent-0.0.1-SNAPSHOT-uber.jar /app/app.jar

# Default role is client, but user can override with arguments or ROLE env var
ENV ROLE=client
ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar \"$@\"" ]
CMD ["--role=${ROLE}"]

