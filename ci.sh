#!/bin/bash

set -e

# Indigo
echo ">>> Indigo"
cd indigo
bash ci.sh
cd ..

# Mill Indigo
echo ">>> Mill-Indigo"
cd mill-indigo
bash build.sh
cd ..

# Examples
echo ">>> Examples"
cd examples
bash ci.sh
cd ..

# Demos
echo ">>> Demos"
cd demos
bash builddemos.sh
cd ..

# IndigoJS
echo ">>> IndigoJS"
cd jsapi
bash build.sh
cd ..

