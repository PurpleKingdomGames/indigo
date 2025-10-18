#!/usr/bin/env bash

set -e

sbt -J-Xmx4G crossCleanAll scalafmtCheckAll crossTestAllNoClean crossLocalPublishNoClean
