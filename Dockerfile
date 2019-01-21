FROM openjdk:8-jdk-alpine

MAINTAINER Johannes Molland <johannes.molland@difi.no>

ARG jarPath
ADD ${jarPath} app.jar

ENV JAVA_OPTS=""

ENTRYPOINT [ "sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]

HEALTHCHECK --interval=30s --timeout=2s --retries=3 CMD wget -qO- "http://localhost:8080/manage/health" || exit 1