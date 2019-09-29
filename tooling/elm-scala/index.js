import { Elm } from './src/Main.elm'

var app = Elm.Main.init({
    node: document.getElementById("main"),
    flags: "Elm Scala"
  });

app.ports.fromElm.subscribe(function(count) {
    console.log("count: " + count);
    console.log("doubled by scala: " + Mailbox.double(count));
    app.ports.toElm.send(Mailbox.double(count));
});
