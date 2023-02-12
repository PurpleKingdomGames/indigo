#!/usr/bin/env bash

# Run from root.

sbt crossCleanAll scalafmtCheck crossTestAllNoClean crossLocalPublishNoClean
