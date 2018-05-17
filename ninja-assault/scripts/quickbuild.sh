#!/usr/bin/env bash

# Expected to be run from the project root like: bash scripts/<script_name>.sh

set -e

HERE=$(pwd)
SOURCE_DIR=$HERE/ninja-assault
GAME_DIR=$HERE/game

cd $SOURCE_DIR

sbt fastOptJS

cd $HERE

echo "Fast"

GAME_FAST_SCRIPT_PATH=$GAME_DIR/fastscripts/ninja-assault-fastopt.js
GAME_FAST_SCRIPT_BUILD_PATH=$SOURCE_DIR/target/scala-2.12/ninja-assault-fastopt.js

if [ -f $GAME_FAST_SCRIPT_PATH ]; then
    rm $GAME_FAST_SCRIPT_PATH
fi

if [ -f "$GAME_FAST_SCRIPT_PATH.map" ]; then
    rm "$GAME_FAST_SCRIPT_PATH.map"
fi

cp $GAME_FAST_SCRIPT_BUILD_PATH $GAME_FAST_SCRIPT_PATH
cp "$GAME_FAST_SCRIPT_BUILD_PATH.map" "$GAME_FAST_SCRIPT_PATH.map"

cd $HERE
