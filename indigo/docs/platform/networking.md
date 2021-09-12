---
id: networking
title: Networking
---

> This page has not yet been reviewed for compatibility with version 0.9.2. Details may now be incorrect.

Indigo supports basic networking via HTTP or WebSockets.

## Network calls and the game loop

JavaScript handles network calls via callbacks and promises, but that doesn't fit comfortably with Indigo's design. Network calls by their nature are side effects, which is bad for testing our game. We'd prefer to declare the _intention_ to make a network call and then have a response in as an argument to a future frame, than to have to make sense of a mid frame interruption.

Therefore, what we have is a side-effecting networking system inside Indigo which you communicate with via the normal event loop. If you want to send an HTTP request you emit a `GET(url, params, headers)` event, and at the beginning of some future frame, hopefully you'll get an `HttpResponse(status, headers, body)`. WebSockets work in a similar way but are complicated by having more states.

For a complete list of network events, please head over the the [events documentation](gameloop/events.md).

We have examples of networking in our indigo-examples repo, of both [HTTP](https://github.com/PurpleKingdomGames/indigo-examples/blob/master/examples/http/src/main/scala/indigoexamples/HttpExample.scala) and [WebSockets](https://github.com/PurpleKingdomGames/indigo-examples/blob/master/examples/websocket/src/main/scala/indigoexamples/WebSocketExample.scala).

The examples make use of buttons which you can find out about on the [UI Components](presentation/ui-components.md) page.
