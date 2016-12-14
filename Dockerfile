FROM openjdk:7
VOLUME /usr/src/myapp
WORKDIR /usr/src/myapp
ENTRYPOINT ./gradlew clean test --debug
