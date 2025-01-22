#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source ../credentials.sh

rm -fr out/

./mill clean
./mill clean mill-indigo[2.13]
./mill mill-indigo[2.13].compile
./mill mill-indigo[2.13].test

./mill mill-indigo[2.13].publishArtifacts

./mill -i \
    mill.scalalib.PublishModule/publishAll \
    --sonatypeCreds "$SONATYPE_USERNAME":"$SONATYPE_PASSWORD" \
    --gpgArgs --passphrase="$PGP_PASSPHRASE",--no-tty,--pinentry-mode,loopback,--batch,--yes,-a,-b \
    --publishArtifacts mill-indigo[2.13].publishArtifacts \
    --readTimeout  3600000 \
    --awaitTimeout 3600000 \
    --release true \
    --signed  true \
    --sonatypeUri https://oss.sonatype.org/service/local \
    --sonatypeSnapshotUri https://oss.sonatype.org/content/repositories/snapshots