#!/usr/bin/env bash

# Run from project root i.e.
# bash scripts/localbuild.sh

# publish the main libs to your ivy local directory
sbt localPublish

# publish the plugin - uses a meta build so is a bit funky
cd project

sbt sbtIndigo/publishLocal

cd ..
