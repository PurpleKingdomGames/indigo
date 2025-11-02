#!/usr/bin/env bash

set -e

# Indigo Plugin + Mill Plugin
echo ">>> Indigo Plugin + Mill Plugin"
cd indigo-plugin
bash build.sh
cd ..

# SBT Indigo
echo ">>> SBT-Indigo"
cd sbt-indigo
bash build.sh
cd ..

# Indigo
echo ">>> Indigo"
cd indigo
bash build.sh
cd ..
