#!/usr/bin/env bash

# Run from root.

set -e

sbt crossCleanAll scalafmtCheck crossTestAllNoClean crossLocalPublishNoClean
