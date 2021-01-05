#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source credentials.sh

SCALA2=2.13
# SCALA3=3.0.0-M3

mill clean mill-indigo[$SCALA2]
mill mill-indigo[$SCALA2].compile
mill mill-indigo[$SCALA2].test
mill -i mill-indigo[$SCALA2].publish --sonatypeCreds $SONATYPE_USERNAME:$SONATYPE_PASSWORD --release true

# mill clean mill-indigo[$SCALA3]
# mill mill-indigo[$SCALA3].compile
# mill mill-indigo[$SCALA3].test
# mill -i mill-indigo[$SCALA3].publish --sonatypeCreds $SONATYPE_USERNAME:$SONATYPE_PASSWORD --release true 
