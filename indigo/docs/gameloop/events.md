---
id: events
title: Events
---

## The event loop

The life-cycle of events in Indigo is very strict so that we can support referential transparency.

Key features of events:

- Events are immutable.
- Events are ordered (First-In-First-Out).
- Events that are present at the start of a frame, are all of the events.
- Events generated during the frame will only become available in the next frame.
- Events only last for a single frame, whether acted on or not, before being disposed of.

> The events list received by the frame is never empty, it always contains one `Frametick` event, and it is always last.

Indigo processes events as follows:

1. The current frame emits events via `Outcome`s. These events are not available to the current frame.
1. Most events are sent to a queue, but some system specific events will be processed and actioned immediately, specifically:
   1. Audio events
   1. Network events
   1. Storage events
   1. Asset load events
1. When the next frame starts, all the messages that went to the queue are retrieved in order, and the `FrameTick` is appended.
1. The events are then used to construct the `InputState` which is included as part of the `FrameContext`.
1. The events are then consumed as follows (note that each stage could create new events to be processed on the next frame):
   1. The events are sent to the model update function in order.
   1. The events are sent to the view model update function in order.
   1. The events are sent to the subsystem update functions in order.
   1. The events are passed to scene elements to process entity events.
1. All events are then discarded.

## `GlobalEvent`s

All events must be tagged with the `GlobalEvent` trait in order for Indigo to process them.

You can create your own events by simply extending `GlobalEvent`.

### System

- `FrameTick` - the last event on a frame, used to update anything that must be updated every frame.
- `ViewportResize(viewport)` - emitted when the game is resized so that your game layout can adapt.

#### Full Screen

- `ToggleFullScreen` - Attempt to enter or exit full screen mode
- `EnterFullScreen` - Attempt to enter full screen mode
- `ExitFullScreen` - Attempt to exit full screen mode
- `FullScreenEntered` - The game entered full screen mode
- `FullScreenEnterError` - A problem occurred trying to enter full screen
- `FullScreenExited` - The game exited full screen mode
- `FullScreenExitError` - A problem occurred trying to exit full screen

### `InputEvent`s

#### `MouseEvent`s

What did the mouse do and at what location?

- `Click(x, y)`
- `MouseUp(x, y)`
- `MouseDown(x, y)`
- `Move(x, y)`

#### `KeyboardEvent`s

- `KeyUp(key)`
- `KeyDown(key)`

### Audio

There is only one audio event used to play one off sound effects, since background music is described on the `SceneUpdateFragment`.

- `PlaySound(audioAssetName, volume)`

### Network events

#### Web socket events

- `ConnectOnly(webSocketConfig)`
- `Open(message, webSocketConfig)`
- `Send(message, webSocketConfig)`
- `Receive(webSocketId, message)`
- `Error(webSocketId, error)`
- `Close(webSocketId)`

#### HTTP events

- `GET(url, params, headers)`
- `POST(url, params, headers, body)`
- `PUT(url, params, headers, body)`
- `DELETE(url, params, headers, body)`
- `HttpError` - Unspecified error
- `HttpResponse(status, headers, body)`

### `StorageEvent`s

Used to load and save data from local storage.

- `Save(key, data)` - Request the serialized data is stored against the given key.
- `Load(key)` - Load the data stored with the given key
- `Delete(key)` - Delete the data stored against the given key
- `DeleteAll` - Delete all stored data
- `Loaded(data)` - Response event when data has been loaded

### `AssetEvent`s

These are the low level events used to load additional assets at runtime. If you want a slightly more sophisticated loading experience, please look at the asset bundle loader sub system.

Note that the `LoadAsset` event is a convenience event, and in fact loads a bundle containing one element, which is why there is no corresponding "`AssetLoadError`" event, just the bundle version.

> The load events also have a `makeAvailable` flag, which if set to false, loads the asset but doesn't add it to the engine. The `available` flag on the response indicates whether the asset has been made available or not.

- `LoadAsset(assetType, optional key, makeAvailable)` - Delete the data stored against the given key
- `LoadAssetBatch(set of assetType, optional key, makeAvailable)` - Delete the data stored against the given key
- `AssetBatchLoaded(optional key, available)` - Delete the data stored against the given key
- `AssetBatchLoadError(optional key)` - Delete the data stored against the given key

### Scenes

- `Next`
- `Previous`
- `JumpTo(sceneName)`

### `AutomataEvent`s

- `Spawn(key, position, lifeSpan, payload)`
- `KillAll` - Remove them all immediately.
- `Cull` - Remove automatons who have reached the end of their time, you don't have to call this manually.

### Rendering

- `RendererDetails(renderingTechnology, clearColor, magnification)` - "renderingTechnology" tells you if Indigo is using WebGL 1.0 or WebGL 2.0.
