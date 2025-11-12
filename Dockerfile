# Multi-stage Dockerfile: build outside or rely on prebuilt artifact
# Stage 0: optional builder (commented out to prefer local build caching)
# FROM maven:3.8.8-eclipse-temurin-17 AS builder
# WORKDIR /workspace
# COPY . /workspace
# RUN mvn -B -Puberjar -DskipTests clean package

# Stage 1: runtime
FROM registry.access.redhat.com/ubi8/openjdk-17@sha256:7016a0c5ce878211a8b82fbeb6619f82a97a35acdba6773be1c5f92f4e85aec8
WORKDIR /app
COPY target/java-load-balancer-*.jar /app/app.jar

# Default role is client, but user can override with arguments or ROLE env var
ENV ROLE=client
ENTRYPOINT ["sh", "-c", "exec java -jar /app/app.jar \"$@\"" ]
CMD ["--role=${ROLE}"]

