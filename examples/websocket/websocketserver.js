import { WebSocketServer } from 'ws';

const wss = new WebSocketServer({ path: "/wsecho", port: 8080 });

wss.on('connection', function connection(ws) {
  ws.on('message', function message(data) {
    console.log('received: %s', data);
    ws.send("echo: " + data);
  });

  ws.send('Connection made...');
});
