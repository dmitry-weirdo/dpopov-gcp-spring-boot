FROM openjdk:8-jdk-alpine
VOLUME /tmp

# declare build arguments (passed from <buildArgs> of dockerfile-maven-plugin configuration
ARG JAR_FILE
ARG GOOGLE_APP_CREDENTIALS_FILE

RUN apk add --no-cache bash

# echo build argument values
RUN echo 'Input build argument values:'
RUN echo 'JAR_FILE:' ${JAR_FILE}
RUN echo 'GOOGLE_APP_CREDENTIALS_FILE:' ${GOOGLE_APP_CREDENTIALS_FILE}

COPY ${GOOGLE_APP_CREDENTIALS_FILE} gcp-credentials.json
COPY ${JAR_FILE} app.jar
ENV GOOGLE_APPLICATION_CREDENTIALS "/gcp-credentials.json"
ENTRYPOINT ["java","-agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n","-jar","/app.jar"]

# ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n","-jar","/app.jar"]

# export GOOGLE_APPLICATION_CREDENTIALS="/home/user/Downloads/[FILE_NAME].json"