#!/usr/bin/env bash

# Run from root.

sbt crossCleanAll crossTestAllNoClean crossLocalPublishNoClean
