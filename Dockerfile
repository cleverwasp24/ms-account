FROM openjdk:17-oracle
ADD target/ms-account-0.0.1-SNAPSHOT.jar ms-account.jar
ENTRYPOINT ["java","-jar","ms-account.jar"]