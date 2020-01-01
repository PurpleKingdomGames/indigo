#!/bin/bash

mkdocs build --clean

http-server -c-1
