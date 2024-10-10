#!/usr/bin/env bash

set -e

sbt crossCleanAll scalafmtCheckAll crossTestAllNoClean crossLocalPublishNoClean
