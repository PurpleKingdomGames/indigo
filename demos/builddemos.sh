#!/bin/bash

cd pirate

sbt clean buildGame

cd ..

cd snake

sbt clean buildGame

cd ..
