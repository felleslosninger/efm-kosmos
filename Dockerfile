FROM openjdk:8-jre-slim

RUN groupadd -o -g 1000 java \
    && useradd -o -r -m -u 1000 -g 1000 java

ENV APP_DIR=/opt/deploymanager \
    JAVA_OPTS=""

ADD /target/*.jar ${APP_DIR}/app.jar

RUN chown -R java:java ${APP_DIR}
RUN chmod +x ${APP_DIR}/*

WORKDIR ${APP_DIR}

USER java

ENTRYPOINT [ "sh", "-c", "exec java $JAVA_OPTS -jar app.jar ${0} ${@}" ]