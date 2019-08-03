#!/bin/bash

set -ex

SANDBOX_PATH=./.js/target/indigo-js

if [[ -f $SANDBOX_PATH/index.html ]]; then

  cp interop.html $SANDBOX_PATH/.

  open -a Firefox http://127.0.0.1:8080/interop.html

  http-server -c-1 $SANDBOX_PATH/
else
  echo "index.html missing - do you need to rebuild sandbox?"
fi
