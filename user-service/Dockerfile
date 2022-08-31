FROM openjdk:20-ea-11
VOLUME /tmp
COPY build/libs/user-service-0.0.1.jar user-service.jar
ENTRYPOINT ["java", "-jar", "user-service.jar"]