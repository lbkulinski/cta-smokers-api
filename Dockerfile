FROM amazoncorretto:25-alpine3.19

WORKDIR /app

ARG JAR_FILE

COPY ${JAR_FILE} app.jar

RUN addgroup -S spring && adduser -S spring -G spring

RUN chown -R spring:spring /app

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
