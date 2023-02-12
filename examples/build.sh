#!/usr/bin/env bash

set -e

sbt cleanAll scalafmtCheckAll testAllNoClean
