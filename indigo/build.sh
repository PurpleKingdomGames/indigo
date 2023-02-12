#!/usr/bin/env bash

# Run from root.

set -e

sbt crossCleanAll scalafmtCheckAll crossTestAllNoClean crossLocalPublishNoClean
