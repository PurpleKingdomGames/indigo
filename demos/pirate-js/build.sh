#!/bin/bash

realpath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}

CWD=$(pwd)
DIR="$(realpath "$( dirname "$0" )")"

rm -rf ~/.ivy2/local/indigo

cd $DIR/../../indigo
./localpublish.sh

cd $DIR/../../jsapi
sbt clean update compile
sbt indigojs/clean indigojs/fastOptJS

rm -rf $DIR/build
cp -r $DIR/src $DIR/build
cp $DIR/../../jsapi/indigojs/target/scala-2.12/indigojs-fastopt.js $DIR/build/js/indigo.min.js

cd $CWD