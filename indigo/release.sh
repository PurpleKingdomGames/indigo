#!/usr/bin/env bash

# Run from root.

set -e

export GPG_TTY=$(tty)

source ../credentials.sh

sbt clean update crossIndigoRelease 
