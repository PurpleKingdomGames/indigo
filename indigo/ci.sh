#!/usr/bin/env bash

cd project

sbt clean sbtIndigo/publishLocal

cd ..

sbt cleanAll testAllNoCleanJS localPublish
