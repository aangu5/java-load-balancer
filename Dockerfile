FROM registry.access.redhat.com/ubi8/openjdk-17@sha256:7016a0c5ce878211a8b82fbeb6619f82a97a35acdba6773be1c5f92f4e85aec8
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080:8080