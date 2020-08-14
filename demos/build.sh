#!/usr/bin/env bash

cd snake

mill snake.fastOpt
mill snake.indigoBuild

cd ..
