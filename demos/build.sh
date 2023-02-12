#!/usr/bin/env bash

set -e

cd snake

mill clean
mill snake.checkFormat
mill snake.test
mill snake.fastOpt
mill snake.indigoBuild

cd ..

cd pirate

sbt scalafmtCheck test buildGame

cd ..
