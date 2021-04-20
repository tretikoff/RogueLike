FROM gradle:6.7 as builder

COPY build.gradle.kts .
COPY src ./src

RUN gradle clean build --no-daemon
FROM openjdk:8-jre-alpine
EXPOSE 8084:8084

COPY --from=builder "/home/gradle/build/libs/gradle-1.0-SNAPSHOT.jar" /server.jar

CMD [ "java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "/server.jar" ]