FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} api-service.jar
ADD .env .env
ENTRYPOINT ["java" , "-jar" ,"api-service.jar"]

EXPOSE 8085
