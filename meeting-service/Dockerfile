FROM openjdk:20-ea-11
VOLUME /tmp
COPY build/libs/meeting-service-0.0.1.jar meeting-service.jar
ENTRYPOINT ["java", "-jar", "meeting-service.jar"]