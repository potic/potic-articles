FROM openjdk:8

RUN mkdir -p /usr/src/potic-articles && mkdir -p /opt

COPY build/distributions/* /usr/src/potic-articles/

RUN unzip /usr/src/potic-articles/potic-articles-*.zip -d /opt/ && ln -s /opt/potic-articles-* /opt/potic-articles

WORKDIR /opt/potic-articles

EXPOSE 8080
ENV ENVIRONMENT_NAME test
ENTRYPOINT [ "sh", "-c", "./bin/potic-articles --spring.profiles.active=$ENVIRONMENT_NAME" ]
CMD []
