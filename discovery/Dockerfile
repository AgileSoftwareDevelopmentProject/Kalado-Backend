FROM openjdk:21


WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 18761

ENTRYPOINT ["java", "-jar", "app.jar"]
