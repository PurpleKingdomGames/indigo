#!/usr/bin/env bash

# Run from root.

sbt crossCleanAll crossTestAllNoClean crossLocalPublishNoClean

cd sandbox

sbt buildGame

cd ../perf

sbt buildGame

cd ..
