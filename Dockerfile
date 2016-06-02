FROM ardoq/java:3.3-8u74
MAINTAINER Kristian Helgesen "<kristian@ardoq.com>"

ADD target/ardoq-maven-addon.jar ardoq-maven-addon.jar
ADD server.yml server.yml

ENV ARDOQ_HOST api
ENV ARDOQ_HOST_PROTOCOL http

EXPOSE 8080

CMD ["java","-Djava.net.preferIPv4Stack=true","-jar","ardoq-maven-addon.jar","server","server.yml"]
