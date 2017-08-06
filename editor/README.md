#Indigo Editor

##TODO
- config edit pane
  - text input component
  - text validation
  - finish config editor fields
  - Bring in bootstrap or something to make it less ugly
- asset edit pane
- game definition edit pane

## Doing the embed.

Scala.js can call JavaScript natively using a [trait based interface](https://www.scala-js.org/doc/interoperability/facade-types.html)

Elm talks to JavaScript via [ports](https://guide.elm-lang.org/interop/javascript.html)

In practical terms I think this means:

The Elm editor is embedded in a static page.

We will need a native JavaScript / TypeScript bridge to act as a go-between for the game framework and the editor. Should be very lean and simple.

The static page defines:
1. A placeholder for the editor
1. A placeholder for the running game
1. Initialisation of the bridge
1. Initialisation code for the editor

The editor triggers the load of the game via the bridge when ready.

The 'bridge' will need to be able to do a few things:
1. Accept editor commands and forward them to Indigo e.g. pause game
1. Accept Indigo events and pass them on to the editor e.g. Sprite X was clicked
1. Receive actions from the editor e.g. Reload game
1. Supply an editor 'ping' so that the framework knows if it's running in the editor

```
Layers of the editor:

-------------
| Editor    |
-------------
| Bridge    |
-------------
| Framework |
-------------
| Indigo    |
-------------
```
