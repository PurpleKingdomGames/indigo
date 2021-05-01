#!/bin/bash

mill clean indigo-plugin[2.12]
mill clean indigo-plugin[2.13]
# mill clean indigo-plugin[3.0.0-RC3]

mill indigo-plugin[2.12].compile
mill indigo-plugin[2.13].compile
# mill indigo-plugin[3.0.0-RC3].compile

mill indigo-plugin[2.12].publishLocal
mill indigo-plugin[2.13].publishLocal
# mill indigo-plugin[3.0.0-RC3].publishLocal
