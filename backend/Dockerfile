FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S aiart && adduser -S aiart -G aiart
WORKDIR /app
COPY --from=build /workspace/target/aiart-platform-*.jar app.jar
RUN mkdir -p /app/uploads && chown -R aiart:aiart /app

USER aiart
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
