# syntax=docker/dockerfile:1.7

# Runtime image for the Spring Batch Dashboard. The released JAR already
# contains the React SPA at classpath:/static/ — the release workflow
# (.github/workflows/release.yml) copies frontend/dist into
# backend/src/main/resources/static before running `mvnw package`, so this
# Dockerfile is just a slim JRE wrapper around the resulting fat JAR.
#
# Build context expects backend/target/*.jar to exist. CI produces it via the
# release workflow; local builds need a `yarn build` (frontend) + `mvnw
# package` (backend, with frontend/dist copied into static/) first.

FROM eclipse-temurin:21-jre AS layer-extract
WORKDIR /build
COPY backend/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 1001 app
USER app

COPY --from=layer-extract /build/dependencies/ ./
COPY --from=layer-extract /build/spring-boot-loader/ ./
COPY --from=layer-extract /build/snapshot-dependencies/ ./
COPY --from=layer-extract /build/application/ ./

EXPOSE 8080
ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]
