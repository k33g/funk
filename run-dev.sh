#!/bin/sh
FUNK_TOKEN="panda" MAVEN_OPTS="-Djava.security.manager -Djava.security.policy=./funk.policy" mvn install exec:java