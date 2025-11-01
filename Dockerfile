# build
FROM gradle:8.9-jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar --no-daemon

# run (경량 JRE + 비루트)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
USER app
COPY --from=build /app/build/libs/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
