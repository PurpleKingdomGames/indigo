# Logging

Sometimes, you just need a log message, and Indigo comes with a very simple logger with a couple of quirks for game dev.

The available logging options are:
```
import IndigoLogger._

consoleLog(...)
info(...)
error(...)
errorOnce(...)
debug(...)
debugOnce(...)
```

## Requirements

Here is the signature of the info logger:

```scala
def info[A](valueA: A)(implicit showA: AsString[A]): Unit
```

Please note that this requires a instance of `AsString` to exist for *any* type you intend to log. For more information please read the entry on [AsString](collections-abstractions-and-typeclasses.md#AsString).

## Format
Logs are currently written out in a fixed format:

`s"""[${system time millis}] [$level] [Indigo] $message"""`

## Useful information

`info`,`error`, and `debug` do exactly what you expect.

`consoleLog` prints to standard out (console.log in JavaScript) without any log message formatting.

### Logging just once..

Games update many times a second, and it's very easy to get flooded with log messages.

To help with that, we've added the `errorOnce` and `debugOnce` methods. These methods keep a cache of what they've logged, and only log the same string once. ***Warning***, that does mean that if every log is unique you'll be filling up memory with log messages.
