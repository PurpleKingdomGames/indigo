#!/bin/bash

cd snake

mill snake.fastOpt
mill snake.indigoBuildJS

cd ..
