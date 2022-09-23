FROM openjdk:20-ea-11
VOLUME /tmp
COPY build/libs/discovery-service-1.0.jar discovery-service.jar
ENTRYPOINT ["java", "-jar", "discovery-service.jar"]