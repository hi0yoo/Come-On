FROM openjdk:20-ea-11
VOLUME /tmp
COPY build/libs/config-service-1.0.jar config-service.jar
ENTRYPOINT ["java", "-jar", "config-service.jar"]