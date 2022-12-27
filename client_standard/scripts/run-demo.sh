#!/bin/bash

source ./common.sh

##########################
#                        #
#   PROJECT PROPERTIES   #
#                        #
##########################

# This is the URL + path of the frontend server, i.e. 'https://1.2.3.4:8000/frontend/recv'
FRONTEND_URL=http://localhost:8000/frontend/recv

# Where to put the Keychain db file
#KEYCHAIN_DB_FOLDER=../data
#KEYCHAIN_DB_FILE=mitm_client.db

# Create folder if it doesn't exist
#[ ! -d ${KEYCHAIN_DB_FOLDER} ] && mkdir -p ${KEYCHAIN_DB_FOLDER}
# File is absolute path
#KEYCHAIN_DB_FILE_PATH=${KEYCHAIN_DB_FOLDER}/${KEYCHAIN_DB_FILE}

WITH_KEYCHAIN=

### TEST CLASSES ###

if [ ! -d "${CLASSES}" ]; then
  echo "Missing classes folder '${CLASSES}'.  Please build and try again."
  exit 1
fi

# Make sure data/ exists, otherwise InitializeDb fails
mkdir -p ${DATA_DIR}
mkdir -p ${LOG_DIR}

### Java Properties ###

# '*' in classpath means "All Jars in this directory"
CLASSPATH_RUN="${CLASSES}:${CLASSPATH}"
# Keychain Java Wrapper loads native libraries, so we must tell java about them
# There are 3 ways, we chose #1 & #2
#   1. Setting 'java.library.path', which only Java knows about (not an environment variable)
#   2. Export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:$KEYCHAIN_LIB, which is an environment variable
#   3. Link the .so files into /usr/lib/, update /etc/ld.so.conf, and sudo ldconfig so they are naturally found (sudo rights req'd)
LIBRARY_PATH="-Djava.library.path=$KEYCHAIN_LIB"
export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:$KEYCHAIN_LIB

### RUN ###

${JAVA} \
    ${LIBRARY_PATH} \
    -classpath "$CLASSPATH_RUN" \
    -Djava.util.logging.SimpleFormatter.format='[%1$tF %1$tT] [%4$-7s] %5$s%n' \
    ${WITH_KEYCHAIN} \
    io.keychain.mitm.Main \
    ${FRONTEND_URL}

