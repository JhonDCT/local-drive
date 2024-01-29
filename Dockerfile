FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN chmod 777 ./mvnw
RUN ./mvnw install -DskipTests
COPY /workspace/app/target/*.jar app.jar

FROM eclipse-temurin:17-jdk-alpine AS run
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8080