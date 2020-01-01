#!/bin/bash

# API Gen
sbt apigen/clean apigen/run

# JS Interface
sbt indigojs/clean indigojs/fastOptJS

# To test
# cd testharness
# npm start
