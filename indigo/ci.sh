#!/usr/bin/env bash

cd project

sbt sbtIndigo/publishLocal

cd ..

sbt testAllJS
