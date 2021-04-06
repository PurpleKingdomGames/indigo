#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source credentials.sh

mill clean indigo-plugin[2.12]
mill clean indigo-plugin[2.13]
# mill clean indigo-plugin[3.0.0-RC2]

mill indigo-plugin[2.12].compile
mill indigo-plugin[2.13].compile
# mill indigo-plugin[3.0.0-RC2].compile

mill indigo-plugin[2.12].test
mill indigo-plugin[2.13].test
# mill indigo-plugin[3.0.0-RC2].test

mill -i indigo-plugin[2.12].publish --sonatypeCreds $SONATYPE_USERNAME:$SONATYPE_PASSWORD --release true
mill -i indigo-plugin[2.13].publish --sonatypeCreds $SONATYPE_USERNAME:$SONATYPE_PASSWORD --release true
# mill -i indigo-plugin[3.0.0-RC2].publish --sonatypeCreds $SONATYPE_USERNAME:$SONATYPE_PASSWORD --release true
