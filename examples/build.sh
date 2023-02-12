#!/usr/bin/env bash

set -e

sbt cleanAll scalafmtCheck testAllNoClean
