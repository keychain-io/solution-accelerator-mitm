#!/bin/bash

# Call this from the root of the Django project, not within scripts (common.sh is referenced as scripts/common.sh)
#
# run-command.sh <COMMAND>
#   Note Keychain will be loaded even if you are not going to use it for the run-demo.sh script!
#

# load common settings and Keychain
. ./scripts/common.sh
[ $? -eq 1 ] && exit 1

echo "Running command $*"
python3 manage.py $*
