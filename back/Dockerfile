# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/app.jar /app/app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
