[![MIT License](https://img.shields.io/github/license/PurpleKingdomGames/indigo?color=indigo)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)
[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Findigo%2Ftags)](https://github.com/PurpleKingdomGames/indigo/releases)
[![Discord Chat](https://img.shields.io/discord/716435281208672356?color=blue&label=discord)](https://discord.com/channels/716435281208672356)

# Indigo

Indigo is a non-traditional, proof of concept game engine written in Scala. It aims to allow programmers to build games using a purely functional set of APIs, focusing on developer productivity and testing.

Currently Indigo only exports web games, we hope to support more platforms in the future.

Documentation can be found on [indigoengine.io](https://indigoengine.io).

## Full local build and test instructions

### Build requirements

You will need:

- Mill
- SBT
- JDK 1.8
- [glslang validator](https://github.com/KhronosGroup/glslang) - can be installed with your favorite package manager.

### Running the build

On Mac / Linux, from the repo root to do a full build and test:

```bash
bash build.sh
```

> Windows users: Most of the things in the script mentioned above should work, but Indigo is not routinely built on Windows machines so we currently offer no guarantees or support. We hope to in the future.

There is also another script which is faster since it doesn't build the examples, demos, or IndigoJS.

```bash
bash localpublish.sh
```
