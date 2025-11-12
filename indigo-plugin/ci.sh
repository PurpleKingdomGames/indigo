#!/usr/bin/env bash

set -e

./mill --no-server --disable-ticker clean
./mill --no-server --disable-ticker -j2 __.compile
./mill --no-server --disable-ticker -j2 __.test
./mill --no-server --disable-ticker -j2 __.checkFormat

# Cannot support Scalafix until we drop Scala 2.12 support, which sbt depends on..
# ./mill --no-server --disable-ticker -j1 __.fix --check

./mill --no-server --disable-ticker -j1 __.publishLocal
