FROM openjdk:alpine

ADD . /home/reservierungssystem/krese

WORKDIR /home/reservierungssystem/krese

RUN sh ./gradlew clean build

RUN cp js/index.html web/index.html

RUN cp js/favicon.ico web/favicon.ico

RUN ls -la .
RUN ls -la common
RUN ls -la js
RUN ls -la jvm

RUN rm -rf doc
RUN rm -rf .gradle

CMD /usr/bin/env java -jar jvm/build/libs/jvm-1.0-SNAPSHOT.jar
