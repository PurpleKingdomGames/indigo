#!/usr/bin/env bash

# Run from root.

set -e

export GPG_TTY=$(tty)

source credentials.sh

cd project

sbt sbtIndigo/clean sbtIndigo/compile sbtIndigo/publishSigned sonatypeBundleRelease

echo ""
echo "Attempting plugin release"
cd ..

# sbt indigoRelease 

# echo ""
# echo "Attempting release"
# sbt sonatypeBundleRelease
