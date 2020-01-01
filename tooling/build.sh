#!/bin/bash

cd scala

sbt clean update compile fastOptJS

cd ..

npm run-script build

# Output is in the `dist` folder
# cd dist
# http-server -c-1
