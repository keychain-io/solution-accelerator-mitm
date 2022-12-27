#!/bin/bash

# environment file should set and export variables like:
#   DBHOST, DBPASS, DBUSER, DBNAME, ISSUER_STANDALONE, KEYCHAIN_DATA_FOLDER, RELEASE_ENV,
# It stays in the HOME directory, and is not versioned
if [ -f "config/.env" ]; then
  . config/.env
fi

# We try 5 ways to determine the Keychain home
# 1. KEYCHAIN_HOME environment variable already set (only set it if you KNOW what you are doing)
# 2. A versioned (decorated) 'keychain-VERSION' folder in the project root
# 3. A plain (undecorated) 'keychain' folder in the project root
# 4. A versioned (decorated) 'keychain-VERSION' folder in the HOME folder
# 5. .keychain folder in the user HOME (this is for legacy purposes)
KEYCHAIN_VERSION=$(cat ./keychain.version)
if [ -z "$KEYCHAIN_HOME" ]; then
  if [ -d "./keychain-${KEYCHAIN_VERSION}" ]; then
      echo "Using decorated folder in installation"
      KEYCHAIN_HOME=./keychain-${KEYCHAIN_VERSION}
  elif [ -d "./keychain" ]; then
    echo "Using undecorated folder in installation"
    KEYCHAIN_HOME=./keychain
  elif [ -d "$HOME/keychain-${KEYCHAIN_VERSION}" ]; then
    echo "Using decorated folder in user HOME"
    KEYCHAIN_HOME=$HOME/keychain-${KEYCHAIN_VERSION}
  elif [ -d "$HOME/.keychain" ]; then
    echo "Using HOME/.keychain (legacy)"
    KEYCHAIN_HOME=$HOME/.keychain
  else
    echo "No Keychain found!  Will not start"
    exit 1
  fi
else
  echo "Using pre-set KEYCHAIN_HOME environment variable"
  echo "Note that it is not good practice to set it outside of a script"
fi

echo "KEYCHAIN_HOME is ${KEYCHAIN_HOME}"

# If libgmp folder exists, add to LD_LIBRARY_PATH
LIBGMP_DIR=${INSTALL_DIR}/libgmp
if [ -d "${LIBGMP_DIR}" ]; then
  echo "Adding local libgmp to LD_LIBRARY_PATH"
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$LIBGMP_DIR/lib
fi
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:${KEYCHAIN_HOME}/lib

echo "LD_LIBRARY_PATH is ${LD_LIBRARY_PATH}"

export KEYCHAIN_HOME
export LD_LIBRARY_PATH
export PYTHONPATH=$PYTHONPATH:${KEYCHAIN_HOME}/lib/python
