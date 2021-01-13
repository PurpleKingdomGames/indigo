#!/usr/bin/env bash

set -e

# Clean up local files
echo ">>> Removing ivy artefacts"
rm -fr ~/.ivy2/local/io.indigoengine/indigo-plugin_2.12
rm -fr ~/.ivy2/local/io.indigoengine/indigo-plugin_2.13
rm -fr ~/.ivy2/local/io.indigoengine/mill-indigo_2.13
rm -fr ~/.ivy2/local/io.indigoengine/sbt-indigo

# Indigo
echo ">>> Indigo Workers"
cd indigo
sbt exportWorkers
cp indigo-render-worker/target/scala-3.0.0-M3/indigo-render-worker-opt.js ../indigo-plugin/indigo-plugin/resources/workers/
cp indigo-render-worker/target/scala-3.0.0-M3/indigo-render-worker-opt.js.map ../indigo-plugin/indigo-plugin/resources/workers/
cp indigo-render-worker/target/scala-3.0.0-M3/indigo-render-worker-opt.js ../indigo-plugin/indigo-plugin/resources/workers/
cp indigo-render-worker/target/scala-3.0.0-M3/indigo-render-worker-opt.js.map ../indigo-plugin/indigo-plugin/resources/workers/
cd ..

# Indigo Plugin
echo ">>> Indigo Plugin"
cd indigo-plugin
bash build.sh
cd ..

# SBT Indigo
echo ">>> SBT-Indigo"
cd sbt-indigo
bash build.sh
cd ..

# Mill Indigo
echo ">>> Mill-Indigo"
cd mill-indigo
bash build.sh
cd ..

