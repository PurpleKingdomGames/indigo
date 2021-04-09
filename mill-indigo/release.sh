#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source credentials.sh

mill clean mill-indigo[2.13]
mill mill-indigo[2.13].compile
mill mill-indigo[2.13].test
mill -i mill-indigo[2.13].publish --sonatypeCreds $SONATYPE_USERNAME:$SONATYPE_PASSWORD --release true
