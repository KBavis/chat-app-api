FROM openjdk:17-jdk
EXPOSE 8080
COPY target/chat-app-api.jar /app/chat-app-api.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "chat-app-api.jar"]
