#!/bin/bash

rm -fr out/

mill clean
mill clean indigo-plugin[2.12]
mill clean indigo-plugin[2.13]

mill indigo-plugin[2.12].compile
mill indigo-plugin[2.13].compile

mill indigo-plugin[2.12].publishLocal
mill indigo-plugin[2.13].publishLocal
