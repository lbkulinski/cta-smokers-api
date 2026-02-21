FROM amazoncorretto:25-alpine3.19

WORKDIR /app

ARG JAR_FILE

COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
