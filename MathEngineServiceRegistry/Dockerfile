FROM openjdk:21-jdk

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8761

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]