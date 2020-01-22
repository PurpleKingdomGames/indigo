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
    if (fontData.fontPath === null)
        return;

    opentype.load(fontData.fontPath, function(e, font) {
        if (e !== null) {
            app.ports.fontProcessErr.send(e.message);
            return;
        }
        let fontSize = fontData.size;

        let x = 0;
        let y = (font.ascender / fontSize) + fontSize;
        let glyphs = [];

        for (let key in font.glyphs.glyphs) {
            if (!font.glyphs.glyphs.hasOwnProperty(key))
                continue;

            let glyph = font.glyphs.glyphs[key];
            if (glyph == null || glyph.unicodes.length === 0)
                continue;

            // Limit to ASCII if defined
            if (fontData.asciiOnly === true && glyph.unicodes.some(e => e > 255))
                continue;

            const boundingBox = glyph.getBoundingBox();
            if (boundingBox.x1 === 0 && boundingBox.x2 === 0 && boundingBox.y1 === 0 && boundingBox.y2 === 0)
                continue;

            glyphs.push(glyph);
        }

        let spritePacker = new SpritePacker();
        let canvasWidth = 16;
        let canvasHeight = 16;

        const sprites = spritePacker.fit(
            glyphs
                .sort(function (glyphA, glyphB) {
                    let maxSideA = Math.max((glyphA.xMax - glyphA.xMin), (glyphA.yMax - glyphA.yMin));
                    let maxSideB = Math.max((glyphB.xMax - glyphB.xMin), (glyphB.yMax - glyphB.yMin));

                    return maxSideA - maxSideB;
                })
                .reverse()
                .map(glyph => {
                    let chars = glyph.unicodes.map(c => String.fromCharCode(c)).join('');
                    let width = font.getAdvanceWidth(chars, fontSize);
                    return {
                        w: width,
                        h: ((font.ascender / font.unitsPerEm) * fontData.size) + ((font.descender / font.unitsPerEm) * fontData.size) + fontData.size,
                        glyph: glyph
                    };
                })
        );

        let canvasEl = document.createElement('canvas');
        let canvas = canvasEl.getContext('2d');
        canvasEl.width = Math.min(spritePacker.root.w, 4096);
        canvasEl.height = Math.min(spritePacker.root.h, 4096);

        sprites.forEach(data => {
            const glyph = data.glyph;
            glyph.draw(canvas, data.fit.x, data.fit.y + fontData.size, fontData.size);
        });

        canvas.save();

        app.ports.fontProcessed.send({
            texture: canvasEl.toDataURL(),
            mapJson: ''
        });
    });
})