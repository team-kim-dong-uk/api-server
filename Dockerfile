FROM openjdk:11-jdk AS builder
# build

WORKDIR application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} spring.jar
RUN java -Djarmode=layertools -jar spring.jar extract

FROM openjdk:11-jre-slim
WORKDIR application
ENV port 8080

COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]




