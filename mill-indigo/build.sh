#!/usr/bin/env bash

set -e

rm -fr out/

./mill clean
./mill clean mill-indigo[2.13]
./mill mill-indigo[2.13].compile
./mill mill-indigo[2.13].test
./mill mill-indigo[2.13].checkFormat
./mill mill-indigo[2.13].publishLocal
