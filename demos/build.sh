#!/usr/bin/env bash

cd snake

mill clean
mill snake.test
mill snake.fastOpt
mill snake.indigoBuild

cd ..

cd pirate

sbt test buildGame

cd ..
