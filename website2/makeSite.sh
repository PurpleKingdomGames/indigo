#!/bin/bash

set -e

WEBSITE_DIR=$(pwd)
PROJECT_DIR=$WEBSITE_DIR/..
INDIGO_ENGINE_DIR=$PROJECT_DIR/indigo/

# -----
# generate / check docs
cd $INDIGO_ENGINE_DIR
sbt gendocs
cd $WEBSITE_DIR

# -----
# build the site
hugo --cleanDestinationDir
