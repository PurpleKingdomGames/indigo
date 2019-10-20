
import { Elm } from "./elm.compiled.js"

var app = Elm.Main.init({
    node: document.querySelector('main'),
    flags: "Indigo Tooling"
  });

app.ports.onDownload.subscribe(function(canvasId) {
    document.getElementById("downloadLink").setAttribute("href", document.getElementById(canvasId).toDataURL("image/png"));
});

app.ports.sendToScalaJS.subscribe(function(msg) {
  app.ports.receiveFromScalaJS.send(ElmMailbox.post(JSON.stringify(msg)));
});
