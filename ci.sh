#!/bin/bash

# Indigo
echo ">>> Indigo"
cd indigo
bash ci.sh
cd ..

# Examples
echo ">>> Examples"
cd examples
bash ci.sh
cd ..

# Demos
echo ">>> Snake"
cd demos/snake
sbt buildGameFull
cd ../..

# Supporting tooling...
# JS API
# Tooling

# Documentation...
# Docs site

