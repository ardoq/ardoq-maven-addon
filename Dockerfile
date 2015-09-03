FROM ardoq/java:3.2-8u51
MAINTAINER Kristian Helgesen "<kristian@ardoq.com>"

ADD target/ardoq-maven-addon.jar ardoq-maven-addon.jar
ADD server.yml server.yml

ENV ARDOQ_HOST api
ENV ARDOQ_HOST_PROTOCOL http

CMD ["java","-Djava.net.preferIPv4Stack=true","-jar","ardoq-maven-addon.jar","server","server.yml"]
