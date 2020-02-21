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

cp $DIR/../../jsapi/indigojs/target/scala-2.13/indigojs-fastopt.js $DIR/build/js/indigo.js
uglifyjs $DIR/build/js/indigo.js --compress 'pure_funcs="F2,F3,F4,F5,F6,F7,F8,F9,A2,A3,A4,A5,A6,A7,A8,A9",pure_getters,keep_fargs=false,unsafe_comps,unsafe' | uglifyjs --mangle --output=$DIR/build/js/indigo.min.js

cd $CWD