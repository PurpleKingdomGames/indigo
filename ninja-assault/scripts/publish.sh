#!/bin/bash

# Expected to be run from the project root like: bash scripts/<script_name>.sh

set -e

HERE=$(pwd)
SOURCE_DIR=$HERE/ninja-assault
GAME_DIR=$HERE/game
SCRIPTS_DIR=$HERE/scripts
CORDOVA_WWW_DIR=$HERE/ninja_build/www

bash scripts/build.sh

rm -fr $CORDOVA_WWW_DIR/*

cp $GAME_DIR/na_full_opt.htm $CORDOVA_WWW_DIR/index.html
cp -r $GAME_DIR/assets $CORDOVA_WWW_DIR
cp -r $GAME_DIR/fullscripts $CORDOVA_WWW_DIR
