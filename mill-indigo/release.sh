#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source credentials.sh

mill clean mill-indigo
mill mill-indigo.compile
mill mill-indigo.test
mill -i mill-indigo.publish --sonatypeCreds $SONATYPE_USERNAME:$SONATYPE_PASSWORD --release true
