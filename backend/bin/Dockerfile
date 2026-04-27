FROM eclipse-temurin:21-jre AS extract
WORKDIR /build
COPY target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 1001 app

USER app

COPY --from=extract /build/dependencies/ ./
COPY --from=extract /build/spring-boot-loader/ ./
COPY --from=extract /build/snapshot-dependencies/ ./
COPY --from=extract /build/application/ ./

EXPOSE 8080

ENTRYPOINT ["java","org.springframework.boot.loader.launch.JarLauncher"]
