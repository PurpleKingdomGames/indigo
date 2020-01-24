#!/bin/bash

# Build the core...
# Indigo
cd indigo
bash ci.sh
cd ..

# Check the core looks ok...
# Demos
# Examples
cd examples
bash ci.sh
cd ..

# Supporting tooling...
# JS API
# Tooling

# Documentation...
# Docs site

