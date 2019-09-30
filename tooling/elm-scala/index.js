import { Elm } from './src/Main.elm'

var app = Elm.Main.init({
    node: document.getElementById("main"),
    flags: "Elm Scala"
  });

app.ports.toScala.subscribe(function(msg) {
    var asString = JSON.stringify(msg);
    console.log("msg: " + asString);
    var got = Mailbox.post(asString);
    console.log("got: " + got);
    app.ports.fromScala.send(got);
});
