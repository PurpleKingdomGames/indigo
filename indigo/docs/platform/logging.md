---
id: logging
title: Logging
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

Sometimes you need a log message to help you debug something, and to help you do that Indigo comes with a very simple / border-line dumb logger.

## Logging

To write to the console in scala.js you can just use the usual Scala `println`, so why use a logger? Well there's one key advantage: Games update many times a second, and it's very easy to get accidentally flooded with log messages.

To help with that, along with the usual logger methods, we've added the `errorOnce` and `debugOnce` methods. These methods keep a cache of what they've logged, and only log the same string one time. So if you've got a busy process you can still can sane logs to read through.

> ***Danger!*** That does mean that if every log is unique you'll not only be flooded with log messages, but you'll also be filling up your memory with cache entries!

The available logging options are:

```scala mdoc
import indigo.IndigoLogger._

consoleLog("message to log")
info("message to log")
error("message to log")
errorOnce("message to log")
debug("message to log")
debugOnce("message to log")
```

## Log Format

Logs are currently written out in a fixed format:

`s"""[${system time millis}] [$level] [Indigo] $message"""`

`info`,`error`, and `debug` are just levels you can log at, although this is purely for visual information as the logger has no configuration to change logging levels.

`consoleLog` prints to standard out (console.log in JavaScript) without any log message formatting.
