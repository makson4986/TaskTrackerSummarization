FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY certs/*.cer /tmp/certs/

RUN for cert in /tmp/certs/*.cer; do \
        keytool -importcert \
            -noprompt \
            -trustcacerts \
            -alias "$(basename "$cert" .cer)" \
            -file "$cert" \
            -cacerts \
            -storepass changeit; \
    done \
    && rm -rf /tmp/certs

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
