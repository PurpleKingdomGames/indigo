#!/usr/bin/env bash

set -e

./mill --no-server --disable-ticker clean
./mill --no-server --disable-ticker -j2 __.compile
./mill --no-server --disable-ticker -j1 __.fastLinkJS
./mill --no-server --disable-ticker -j2 __.test
./mill --no-server --disable-ticker -j2 __.checkFormat
./mill --no-server --disable-ticker -j1 __.fix --check