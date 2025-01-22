#!/usr/bin/env bash

set -e

export GPG_TTY=$(tty)

source ../credentials.sh

rm -fr out/

./mill clean
./mill clean indigo-plugin[2.12]
./mill clean indigo-plugin[2.13]

./mill indigo-plugin[2.12].compile
./mill indigo-plugin[2.13].compile

./mill indigo-plugin[2.12].test
./mill indigo-plugin[2.13].test

# Build all artifacts
./mill indigo-plugin[2.12].publishArtifacts
./mill indigo-plugin[2.13].publishArtifacts

# Publish all artifacts
./mill -i \
    mill.scalalib.PublishModule/publishAll \
    --sonatypeCreds "$SONATYPE_USERNAME":"$SONATYPE_PASSWORD" \
    --gpgArgs --passphrase="$PGP_PASSPHRASE",--no-tty,--pinentry-mode,loopback,--batch,--yes,-a,-b \
    --publishArtifacts indigo-plugin[2.12].publishArtifacts \
    --readTimeout  3600000 \
    --awaitTimeout 3600000 \
    --release true \
    --signed  true \
    --sonatypeUri https://oss.sonatype.org/service/local \
    --sonatypeSnapshotUri https://oss.sonatype.org/content/repositories/snapshots

./mill -i \
    mill.scalalib.PublishModule/publishAll \
    --sonatypeCreds "$SONATYPE_USERNAME":"$SONATYPE_PASSWORD" \
    --gpgArgs --passphrase="$PGP_PASSPHRASE",--no-tty,--pinentry-mode,loopback,--batch,--yes,-a,-b \
    --publishArtifacts indigo-plugin[2.13].publishArtifacts \
    --readTimeout  3600000 \
    --awaitTimeout 3600000 \
    --release true \
    --signed  true \
    --sonatypeUri https://oss.sonatype.org/service/local \
    --sonatypeSnapshotUri https://oss.sonatype.org/content/repositories/snapshots