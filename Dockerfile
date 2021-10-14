FROM openjdk:11-jdk AS builder
# build

WORKDIR application
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} spring.jar
ARG SCRIPT_FILE=scripts/*.sh
COPY ${SCRIPT_FILE} ./
RUN java -Djarmode=layertools -jar spring.jar extract
RUN cp $JAVA_HOME/lib/security/cacerts cacerts
RUN bash register_certificate.sh

FROM openjdk:11-jre-slim
WORKDIR application
ENV port 8080

COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
COPY --from=builder application/cacerts ./
ENTRYPOINT ["java", "-Djavax.net.ssl.trustStorePassword=changeit", "-Djavax.net.ssl.trustStore=./cacerts", "org.springframework.boot.loader.JarLauncher"]