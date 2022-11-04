FROM openjdk:20-ea-11
VOLUME /tmp
COPY build/libs/meeting-service-1.0.3.jar meeting-service.jar
ENTRYPOINT ["java", "-jar", "meeting-service.jar"]