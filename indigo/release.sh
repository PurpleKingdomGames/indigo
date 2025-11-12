#!/usr/bin/env bash

# Run from root.

set -e

export GPG_TTY=$(tty)

source ./credentials.sh

./mill clean
./mill __.compile
./mill __.test
./mill __.checkFormat
./mill __.fix --check
./mill -j2 __.fastLinkJS
./mill __.publishSonatypeCentral
