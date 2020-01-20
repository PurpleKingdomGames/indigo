'use strict';
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

app.ports.processFont.subscribe(function(fontData) {
    opentype.load(fontData.fontPath, function(e, font) {
        if (e !== null) {
            console.log(e);
            app.ports.fontProcessErr.send(e);
            return;
        }

        let canvasEl = document.createElement('canvas');
        let canvas = canvasEl.getContext('2d');
        let fontSize = 32;

        let x = 0;
        let y = (font.ascender / fontSize) + fontSize;

        for (let key in font.glyphs.glyphs) {
            if (!font.glyphs.glyphs.hasOwnProperty(key)) {
                continue;
            }
            let glyph = font.glyphs.glyphs[key];
            let char = String.fromCharCode(glyph.unicode);
            if (glyph == null || glyph.unicode === undefined)
                continue;

            let boundingBox = glyph.getBoundingBox();
            if (boundingBox.x1 === 0 && boundingBox.x2 === 0 && boundingBox.y1 === 0 && boundingBox.y2 === 0)
                continue;

            let width = font.getAdvanceWidth(char, fontSize);
            if (x + width > 1024) {
                y += (font.ascender / fontSize) + (font.descender / fontSize) + fontSize;
                x = 0;
            }

            glyph.draw(canvas, x, y, fontSize);
            x += width;
        }

        canvas.save();
        app.ports.fontProcessed.send({
            texture: canvasEl.toDataURL(),
            mapJson: ''
        });
    });
})