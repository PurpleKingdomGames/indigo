#!/bin/bash

# Expected to be run from the project root like: bash scripts/<script_name>.sh

# This is really just to give you some idea of how to set up the project,
# but the build will fail if you don't have indigo, and if you've got indigo
# then you've got SBT etc.

HERE=$(pwd)
SOURCE_DIR=$HERE/ninja-assault
SCRIPTS_DIR=$HERE/scripts

brew install sbt

npm install -g cordova

cd $SOURCE_DIR

npm install source-map
npm install source-map-support

cd $HERE

bash $SCRIPTS_DIR/build.sh

cd $HERE
