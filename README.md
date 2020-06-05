[![MIT License](https://img.shields.io/github/license/PurpleKingdomGames/indigo?color=indigo)](https://github.com/tterb/atomic-design-ui/blob/master/LICENSEs)
[![Latest Tagged Release](https://img.shields.io/badge/dynamic/json?color=purple&label=Latest%20release&query=%24%5B0%5D.name&url=https%3A%2F%2Fapi.github.com%2Frepos%2FPurpleKingdomGames%2Findigo%2Ftags)]()

# Indigo

Indigo is a non-traditional, proof of concept game engine written in Scala. It aims to allow programmers to build games using a purely functional set of APIs. 

> "I always wanted to be a 90's games programmer, valiantly hacking away in a spare room producing wonderous gaming experiences and worlds for people to get lost in ...although I've had to update the dream with a MacBook Pro and Scala."

Currently Indigo only exports web games, we hope to support more platforms in the future.

## Local build instructions

Assuming a machine with the JDK (1.8), SBT, and Mill installed...

On Mac / Linux from the project root:

```bash
bash ci.sh
```

Stand well back.

Windows users: Most of the things in the script above should work, but Indigo is not routinely built on Windows machines so we currently offer no guarantees or support.
