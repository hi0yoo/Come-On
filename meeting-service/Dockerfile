FROM openjdk:11-ea-jre
VOLUME /tmp
COPY build/libs/meeting-service-0.0.1-SNAPSHOT.jar meeting-service.jar
ENTRYPOINT ["java", "-jar", "meeting-service.jar"]