#!/bin/bash

elm make src/ToolsApp.elm --output build/indigotools.js

# open build/index.html
http-server -c-1 build/
