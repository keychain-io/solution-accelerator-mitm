#!/bin/bash

KEYCHAIN_HOME=${KEYCHAIN_HOME:-$HOME/.keychain}
KEYCHAIN_LIB=${KEYCHAIN_HOME}/lib

## On my machine, JAVA_HOME must be set
## Uncomment and set if needed
#export JAVA_HOME=/usr/lib/jvm/jdk-15.0.2

JAVA=${JAVA_HOME}/bin/java
JAVAC=${JAVA_HOME}/bin/javac
if [[ -z "$JAVA_HOME" ]]; then
  # try to find java/javac with 'which'
  JAVA=$(which java)
  JAVAC=$(which javac)

  if [[ -z "$JAVA" || -z "$JAVAC" ]]; then
    echo "JAVA_HOME not set.  Please set this variable to the location of a valid JDK >= 11"
    echo "  Example:   JAVA_HOME=/path/to/jdk $0 [NAME]"
    echo "  Example:   (in your bashrc put 'JAVA_HOME=/path/to/jdk' and restart shell)"
    echo "             $0 [NAME]"
    exit 1
  fi
fi


APPNAME=KeychainMITMClient
PROJECT_DIR=..
SOURCES=${PROJECT_DIR}/src
CLASSES=${PROJECT_DIR}/target/classes
# Project lib folder has JSON, and Keychain lib folder has java/keychain.jar
CLASSPATH="$PROJECT_DIR/lib/*:$KEYCHAIN_LIB/java/*"
DATA_DIR="${PROJECT_DIR}/data/"
LOG_DIR="${PROJECT_DIR}/logs/"


export KEYCHAIN_HOME
export KEYCHAIN_LIB
export JAVA
export JAVAC
export APPNAME
export PROJECT_DIR
export SOURCES
export CLASSES
export CLASSPATH
export DATA_DIR
export LOG_DIR

