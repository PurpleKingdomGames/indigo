#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source ../credentials.sh

echo "Attempting plugin release"
sbt clean update compile publishSigned sonatypeBundleRelease
