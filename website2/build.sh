#!/bin/bash

set -e

WEBSITE_DIR=$(pwd)
PROJECT_DIR=$WEBSITE_DIR/..
INDIGO_ENGINE_DIR=$PROJECT_DIR/indigo/

# -----
# generate / check docs
#  - "indigo/doc" // Docs in ./indigo/target/scala-3.3.0/unidoc/
#  - "docs/mdoc"  // Docs in ./indigo/indigo-docs/target/mdoc
cd $INDIGO_ENGINE_DIR
sbt gendocs
cd $WEBSITE_DIR

# -----
# build the site // $WEBSITE_DIR/target/docs/site
sbt clean laikaSite

# -----
# Publish
mkdir -p target/docs/site/api/
cp -R $INDIGO_ENGINE_DIR/target/scala-3.*/unidoc/. $WEBSITE_DIR/target/docs/site/api/
sbt makeSite ghpagesPushSite
