#!/bin/bash

# Indigo
echo ">>> Indigo"
cd indigo
bash localpublish.sh
cd ..

# Examples
echo ">>> Examples"
cd examples
bash localbuild.sh
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

