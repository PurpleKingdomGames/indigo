#!/bin/bash

# TODO
# Auto update the demos?

set -e

WEBSITE_DIR=$(pwd)
PROJECT_DIR=$WEBSITE_DIR/..
INDIGO_ENGINE_DIR=$PROJECT_DIR/indigo/

# -----
# generate / check docs
#  - "indigo/doc" // Docs in ./indigo/target/scala-3.1.2/unidoc/
#  - "docs/mdoc"  // Docs in ./indigo/indigo-docs/target/mdoc
cd $INDIGO_ENGINE_DIR
sbt gendocs
cd $WEBSITE_DIR

# -----
# build the site
yarn run build

# -----
# Publish
cp -R $INDIGO_ENGINE_DIR/target/scala-3.*/unidoc/ $WEBSITE_DIR/build/indigo-site/api/
sbt clean makeSite ghpagesPushSite
