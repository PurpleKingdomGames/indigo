#!/bin/bash

set -e

./mill clean
./mill __.compile
./mill __.test
./mill __.checkFormat
./mill __.publishLocal
