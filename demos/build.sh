#!/usr/bin/env bash

cd snake

mill snake.test
mill snake.fastOpt
mill snake.indigoBuild

cd ..

cd pirate

sbt test buildGame

cd ..
