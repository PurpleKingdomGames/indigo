#!/usr/bin/env bash

set -e

sbt clean scalafmtCheck sbtIndigo/publishLocal
