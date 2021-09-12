---
id: loading-and-saving-data
title: Loading & Saving Data
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

Sooner or later, you're probably going to need to load data into your game or save user data.

### Dealing with save games

When you think of loading and saving, probably the first thing that comes to mind is save games.

Currently Indigo's local storage options are a bit limited, but what you can do is [storage events](/docs/gameloop/events#storageevents).

Storage events use your browsers local storage, which you can think of like a key value store. You process might be:

1. Organise save data into a case class
2. Convert the data to JSON
3. Fire off a `Save("save game 1", json)` event to stash it away against a key.
4. Then to reload, fire a `Load("save game 1")` event.
5. Eventually (normally 1 frame later) you'll get a `Loaded(json)` event.
6. Deserialise the JSON into your case class and apply it to your model.

### Loading local data

You can always load data in plain text format (that could be JSON or CSV for example), by using making use of the [asset loading](/docs/platform/assets#asset-loading) functionality. This allows you to dynamically load a data file at any time, process it, and apply it to you model.

### Loading and saving over a network

The other way to load and save data might be over a network, please see the [networking page for details](platform/networking.md).

Similar to the save game flow, you might do something like:

1. Organise save data into a case class
2. Convert the data to JSON
3. Fire off a `POST` event to send it to a server over HTTP.
4. Then to reload, fire a `GET` event.
5. Eventually you'll get a `HttpResponse` event.
6. Deserialise the body of the response into your case class and apply it to your model.
