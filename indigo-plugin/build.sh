#!/bin/bash

set -e

./mill clean
./mill __.compile
./mill __.test
./mill __.checkFormat

# Cannot support Scalafix until we drop Scala 2.12 support, which sbt depends on..
# ./mill __.fix --check

./mill __.publishLocal
