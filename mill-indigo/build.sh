#!/usr/bin/env bash

set -e

rm -fr out/

./mill clean
./mill __.compile
./mill __.test
./mill __.checkFormat
./mill __.publishLocal