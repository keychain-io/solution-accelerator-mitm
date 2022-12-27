#!/bin/bash

# Call this from the root of the Django project, not within scripts (common.sh is referenced as scripts/common.sh)
#
# run-demo.sh <PORT> <SUFFIX> <KEYCHAIN FLAG>
#   where KEYCHAIN FLAG is anything - empty means no Keychain, non-empty means use Keychain
#
#   Example: run-demo.sh 8000 12345k
#            run-demo.sh 9000 12345k yes
#

PORT_LISTEN=${1:-8001}
shift
TD_SUFFIX=${1:-}
shift
WITH_KEYCHAIN=${1:-}

# load common settings
TYPE=
if [ -n "$WITH_KEYCHAIN" ]; then
  . ./scripts/common.sh
  [ $? -eq 1 ] && exit 1
  TYPE=_keychain
fi

export TD_SUFFIX

SERVER=back

python3 manage.py runserver --noreload 0:${PORT_LISTEN} --settings ${SERVER}.settings${TYPE}
