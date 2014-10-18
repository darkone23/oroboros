FROM java:8
COPY target/oroboros-0.1.0-SNAPSHOT-standalone.jar /etc/oroboros/o.jar
WORKDIR /etc/oroboros/configs
