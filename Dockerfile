FROM maven:3.9.2-eclipse-temurin-17-alpine as builder

COPY ./src src/
COPY ./pom.xml pom.xml

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
COPY --from=builder target/*.jar Peaksoft_telegram_bot-0.0.1-SNAPSHOT.jar
EXPOSE 8080
CMD ["java","-jar","/Peaksoft_telegram_bot-0.0.1-SNAPSHOT.jar"]