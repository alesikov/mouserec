FROM gradle:7.2-jdk16 as build
WORKDIR /var/server
ADD . .
RUN gradle clean :bootJar --no-daemon

FROM openjdk:16-jdk-alpine as run
COPY --from=build /var/server/build/libs/mouserec-SNAPSHOT.jar /opt/mouserec.jar
EXPOSE 9999
ENTRYPOINT ["java","-jar","/opt/mouserec.jar"]
