FROM openjdk:8

RUN mkdir -p /usr/src/potic-articles && mkdir -p /usr/app

COPY build/distributions/* /usr/src/potic-articles/

RUN unzip /usr/src/potic-articles/potic-articles-*.zip -d /usr/app/ && ln -s /usr/app/potic-articles-* /usr/app/potic-articles

WORKDIR /usr/app/potic-articles

EXPOSE 8080
ENV ENVIRONMENT_NAME test
ENTRYPOINT [ "sh", "-c", "./bin/potic-articles --spring.profiles.active=$ENVIRONMENT_NAME" ]
CMD []
