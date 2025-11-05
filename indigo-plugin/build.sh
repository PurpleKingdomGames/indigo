#!/bin/bash

set -e

./mill clean
./mill __.compile
./mill -j1 __.fastLinkJS
./mill __.test
./mill __.checkFormat
./mill __.publishLocal
