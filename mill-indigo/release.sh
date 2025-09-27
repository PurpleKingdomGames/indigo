#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source ./credentials.sh

rm -fr out/

./mill clean
./mill __.compile
./mill __.test
./mill __.checkFormat
./mill __.publishSonatypeCentral