import { Elm } from './src/Main.elm'

var app = Elm.Main.init({
    node: document.getElementById("main"),
    flags: "Elm Scala"
  });

app.ports.toScala.subscribe(function(msg) {
    app.ports.fromScala.send(Mailbox.post(JSON.stringify(msg)));
});
