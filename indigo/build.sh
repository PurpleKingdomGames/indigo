#!/usr/bin/env bash

# Run from root.

cd project

sbt clean sbtIndigo/publishLocal

cd ..

sbt cleanAll testAllNoCleanJS localPublishNoClean
