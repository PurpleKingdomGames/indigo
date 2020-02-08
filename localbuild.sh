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
echo ">>> Demos"
cd demos
bash builddemos.sh
cd ..

# Supporting tooling...
# JS API
# Tooling

# Documentation...
# Docs site

