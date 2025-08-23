#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source ./credentials.sh

rm -fr out/

./mill clean
./mill clean indigo-plugin[2.12]
./mill clean indigo-plugin[2.13]
./mill clean indigo-plugin[3]

./mill indigo-plugin[2.12].compile
./mill indigo-plugin[2.13].compile
./mill indigo-plugin[3].compile

./mill indigo-plugin[2.12].test
./mill indigo-plugin[2.13].test
./mill indigo-plugin[3].test

# Build all artifacts
./mill indigo-plugin[2.12].publishSonatypeCentral
./mill indigo-plugin[2.13].publishSonatypeCentral
./mill indigo-plugin[3].publishSonatypeCentral
