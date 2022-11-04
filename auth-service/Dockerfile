FROM openjdk:20-ea-11
VOLUME /tmp
COPY build/libs/auth-service-1.0.3.jar auth-service.jar
ENTRYPOINT ["java", "-jar", "auth-service.jar"]