#!/usr/bin/env bash

mill clean mill-indigo[2.13]
# mill clean mill-indigo[3.0.0-RC2]

mill mill-indigo[2.13].compile
# mill mill-indigo[3.0.0-RC2].compile

mill mill-indigo[2.13].publishLocal
# mill mill-indigo[3.0.0-RC2].publishLocal
