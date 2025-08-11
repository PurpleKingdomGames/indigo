#!/bin/bash

set -e

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
./mill indigo-plugin[2.12].checkFormat
./mill indigo-plugin[2.13].checkFormat
./mill indigo-plugin[3].checkFormat
./mill indigo-plugin[2.12].publishLocal
./mill indigo-plugin[2.13].publishLocal
./mill indigo-plugin[3].publishLocal
