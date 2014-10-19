FROM java:8
COPY target/oroboros-0.1.1-SNAPSHOT-standalone.jar /etc/oroboros/o.jar
WORKDIR /etc/oroboros/configs
ENV PORT 80
CMD [ "java", "-jar", "/etc/oroboros/o.jar" ]
