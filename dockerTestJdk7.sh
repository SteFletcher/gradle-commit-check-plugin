#!/bin/bash
docker run --rm -v "$PWD":/usr/src/myapp -v "$HOME/.gradle":/root/.gradle -w /usr/src/myapp openjdk:7 ./gradlew clean test integTest
docker run --rm -v "$PWD":/usr/src/myapp -v "$HOME/.gradle":/root/.gradle -w /usr/src/myapp openjdk:8 ./gradlew clean test integTest