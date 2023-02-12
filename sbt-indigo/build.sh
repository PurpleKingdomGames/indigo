#!/usr/bin/env bash

set -e

sbt clean scalafmtCheckAll sbtIndigo/publishLocal
