import { Elm } from './src/Main.elm'

var app = Elm.Main.init({
    node: document.getElementById("main"),
    flags: "Elm Scala"
  });

app.ports.sendToScalaJS.subscribe(function(msg) {
    app.ports.receiveFromScalaJS.send(ElmMailbox.post(JSON.stringify(msg)));
});
