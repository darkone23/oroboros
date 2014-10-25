FROM java:8
ADD target/server-0.2.0-SNAPSHOT-standalone.jar /etc/oroboros/o.jar
WORKDIR /etc/oroboros/conf.d
ENV PORT 80
CMD [ "java", "-jar", "/etc/oroboros/o.jar" ]
