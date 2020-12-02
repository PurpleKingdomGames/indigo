#!/usr/bin/env bash

SCALA2=2.13
SCALA3=3.0.0-M2

mill clean mill-indigo[$SCALA2]
mill mill-indigo[$SCALA2].compile
mill mill-indigo[$SCALA2].publishLocal

# mill clean mill-indigo[$SCALA3]
# mill mill-indigo[$SCALA3].compile
# mill mill-indigo[$SCALA3].publishLocal
