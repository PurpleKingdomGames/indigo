#!/usr/bin/env bash

# Run from root.

cd project

sbt clean sbtIndigo/publishSigned

cd ..

sbt indigoRelease 

echo ""
echo "Attempting release"
sbt sonatypeBundleRelease
