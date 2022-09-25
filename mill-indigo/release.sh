#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source ../credentials.sh

rm -fr out/

mill clean

mill clean mill-indigo[2.13]

mill mill-indigo[2.13].compile

mill mill-indigo[2.13].test

# mill -i mill-indigo[2.13].publish --sonatypeCreds $SONATYPE_USERNAME:$SONATYPE_PASSWORD --release true

mill mill.scalalib.PublishModule/publishAll \
        mill-indigo[2.13].publishArtifacts \
        $SONATYPE_USERNAME:$SONATYPE_PASSWORD \
        --gpgArgs --passphrase=$PGP_PASSPHRASE,--batch,--yes,-a,-b \
        --release true
