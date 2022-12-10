FROM docker.io/tomcat:10-jdk17-openjdk
WORKDIR /usr/local/tomcat/webapps
ADD ./target/scc2223-backend-1.0.war ROOT.war
EXPOSE 8080
