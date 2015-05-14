FROM tomcat:8-jre8
MAINTAINER Mariusz Jozala "http://aetas.pl"

ENV VIRTUAL_HOST="auth.taskroo.local"
ENV JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/urandom"

RUN rm -rf webapps/*
ADD taskroo-authentication.war $CATALINA_HOME/webapps/ROOT.war
