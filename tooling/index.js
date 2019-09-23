import { Elm } from './src/ToolsApp.elm'

var app = Elm.Main.init({
    node: document.querySelector('main'),
    flags: "Indigo Tooling"
  });

app.ports.onDownload.subscribe(function(canvasId) {
    console.log(canvasId);
    // window.location.href = document.getElementById(canvasId).toDataURL("image/png");
    document.getElementById("downloadLink").setAttribute("href", document.getElementById(canvasId).toDataURL("image/png"));
});
