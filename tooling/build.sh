#!/bin/bash
if !(hash elm 2>/dev/null;) then
    echo "Please install elm before continuing (https://guide.elm-lang.org/install.html)"
    exit 1
fi

echo "Compiling scripts..."
if [ "$1" = "--release" ] ; then
    if !(elm make src/Main.elm --output=./elm.compiled.js --optimize) then
        exit 1
    fi

    if !(uglifyjs ./elm.compiled.js --compress 'pure_funcs="F2,F3,F4,F5,F6,F7,F8,F9,A2,A3,A4,A5,A6,A7,A8,A9",pure_getters,keep_fargs=false,unsafe_comps,unsafe' | uglifyjs --mangle --output=./.temp-build/js/main.min.js) then
        exit 1
    fi
else
    if !(elm make src/Main.elm --output=./elm.compiled.min.js) then
        exit 1
    fi
fi
echo "Done compiling!"