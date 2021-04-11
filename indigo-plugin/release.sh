#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source credentials.sh

mill clean indigo-plugin[2.12]
mill clean indigo-plugin[2.13]

mill indigo-plugin[2.12].compile
mill indigo-plugin[2.13].compile

mill indigo-plugin[2.12].test
mill indigo-plugin[2.13].test

mill mill.scalalib.PublishModule/publishAll \
        indigo-plugin[2.12].publishArtifacts \
        $SONATYPE_USERNAME:$SONATYPE_PASSWORD \
        --gpgArgs --passphrase=$GPG_PASSWORD,--batch,--yes,-a,-b \
        --release true

mill mill.scalalib.PublishModule/publishAll \
        indigo-plugin[2.13].publishArtifacts \
        $SONATYPE_USERNAME:$SONATYPE_PASSWORD \
        --gpgArgs --passphrase=$GPG_PASSWORD,--batch,--yes,-a,-b \
        --release true
