FROM openjdk:21-jdk

EXPOSE 8080

COPY ./target/*.jar /usr/app/inboxview-app-webflux.jar

WORKDIR /usr/app

ENTRYPOINT [ "java", "-jar", "inboxview-app-webflux.jar" ]