#!/usr/bin/env bash

# Run from root.

sbt cleanAll testAllNoClean localPublishNoClean
