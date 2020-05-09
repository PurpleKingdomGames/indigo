#!/bin/bash

cd pirate

sbt clean buildGame

cd ..

cd snake

mill snake.fastOpt
mill snake.indigoBuildJS

cd ..
