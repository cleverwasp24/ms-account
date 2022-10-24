FROM openjdk:17-oracle
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} ms-account.jar
ENTRYPOINT ["java","-jar","/ms-account.jar"]