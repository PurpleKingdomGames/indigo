#!/usr/bin/env bash

set -e

./mill clean
./mill __.compile
./mill -j1 __.fastLinkJS
./mill -j2 __.test
./mill -j2 __.checkFormat
./mill -j1 __.fix --check
./mill __.publishLocal