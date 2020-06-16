#!/usr/bin/env bash

# Run from root.

sbt cleanAll testAllNoCleanJS localPublishNoClean
