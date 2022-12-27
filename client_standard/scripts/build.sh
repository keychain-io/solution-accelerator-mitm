#!/bin/bash

source ./common.sh

### CLEAN CLASSES and CREATE DATA ###

mkdir -p ${CLASSES}
# commented out in case CLASSES is undefined - you don't want to 'rm -rf /*' :)
#rm -rf ${CLASSES}/*

### COMPILE ###
${JAVAC} \
    -Xlint:deprecation \
    -classpath "$CLASSPATH" \
    -sourcepath ${SOURCES} \
    -d ${CLASSES} \
    $(find ${SOURCES} -name "*.java")

