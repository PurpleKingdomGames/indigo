# WebSockets

This is an example of WebSockets.

There are three parts you can look at.

1. `websocketserver.js`
2. `websocket.html`
3. The example/game itself

To run the example, first we need to start a websocket server so we have something to connect to, and we're using a node.js implementation called [ws](https://github.com/websockets/ws) for testing purposes. In this directory, run:

```sh
$ npm install
```

You can then run the service as follows:

```sh
$ node websocketserver.js
```

If you look inside `websocketserver.js` you can see that this is a _very_ simple server. All that is does is echo back whatever text is sent to `ws://localhost:8080/wsecho`.

Now that it's running, I've included two ways to check it.

First, you can just open `websocket.html` in a browser and check the console logs. Again this is super simple, it's sole purpose is to reassure you that your your server is running as expected.

Finally the game, run as normal from example project root:

```sh
$ sbt websocket/fastOptJS websocket/indigoRun
```

Press the button once and the connection is established, press it again to send "Hello!". The results are printed in the logs and on the screen.
