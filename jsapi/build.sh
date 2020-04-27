#!/bin/bash

# API Gen
mill clean apigen
mill apigen.compile
mill apigen.test
mill apigen.run

# JS Interface
mill clean indigojs
mill indigojs.compile
mill indigojs.fastOpt # For the quick JS file.
#mill indigojs.fullOpt

