#!/bin/bash
# Expected to be run from the project root like: bash scripts/<script_name>.sh

set -e

HERE=$(pwd)
CORDOVA_DIR=$HERE/ninja_build

cd $CORDOVA_DIR

cordova run osx

cd $HERE
