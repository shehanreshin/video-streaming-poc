#!/bin/bash
set -e

SECRETS_DIR=command/config

if [ -d "$SECRETS_DIR" ]; then
  for f in "$SECRETS_DIR"/*; do
    if [ -f "$f" ]; then
      varname=$(basename "$f" | tr '[:lower:]' '[:upper:]')
      export "$varname"="$(cat "$f")"
      echo "Loaded secret $varname"
    fi
  done
else
  echo "Secrets directory $SECRETS_DIR does not exist!"
fi

echo "Environment vars loaded:"
env

exec java -jar /app/videostreamingpoc.jar