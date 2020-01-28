'use strict';
const app = Elm.Main.init({
    node: document.querySelector('main'),
    flags: "Indigo Tooling"
  });

/**
 * Draws the specified font to a canvas and informs Elm once done
 *
 * @param {!Font} font
 * @param {!number} padding
 * @param {!number} size
 * @param {?number[]} specificGlyphs
 */
function drawFontMap(font, padding, size, specificGlyphs) {
    const doublePadding = padding * 2;
    const fontMap = {
        name: font.tables.name.fullName.en,
        size: size,
        padding: padding,
        glyphs: []
    }

    const spritePacker = new SpritePacker();
    const fontHeight = Math.round(
        ((font.ascender / font.unitsPerEm) * size) + // Ascender in px
        ((font.descender / font.unitsPerEm) * size) + // Descender in px
        size
    );

    // Map selected glyphs to a width and height to be packed by the sprite packer
    const sprites = spritePacker.fit(
        getGlyphs(font.glyphs.glyphs, specificGlyphs)
            .map(glyph => {
                const chars = glyph.unicodes.map(c => String.fromCodePoint(c)).join('');
                const width = Math.round(font.getAdvanceWidth(chars, size));

                return {
                    w: width + doublePadding,
                    h: fontHeight + doublePadding,
                    char: chars,
                    glyph: glyph
                };
            })
    );

    let canvasEl = document.createElement('canvas');
    let canvas = canvasEl.getContext('2d');

    // Clamp the canvas width and height between 16px and 4096px
    canvasEl.width = Math.max(16, Math.min(spritePacker.root.w, 4096));
    canvasEl.height = Math.max(16, Math.min(spritePacker.root.h, 4096));

    // Draw each glyph to the canvas and push the data to the font map object
    sprites.forEach(data => {
        const x = data.fit.x + padding;
        const y = data.fit.y + padding;

        data.glyph.draw(canvas, x, y + size, size);

        fontMap.glyphs.push({
            unicode: data.glyph.unicode,
            char: data.char,
            x: x,
            y: y,
            w: data.w - doublePadding,
            h: data.h - doublePadding
        });
    });

    // Save the canvas
    canvas.save();

    // Let Elm know we've finished processing and have a texture and map data to use
    app.ports.fontProcessed.send({
        texture: canvasEl.toDataURL(),
        mapJson: JSON.stringify(fontMap, null, 4),
        fontName: fontMap.name
    });
}

/**
 * Grabs the selected glyphs from the glyph array and returns them
 * sorted in width/height descending order
 *
 * @param {!Glyph[]} glyphs
 * @param {?number[]} specificGlyphs
 * @returns {!Glyph[]}
 */
function getGlyphs(glyphs, specificGlyphs) {
    const glyphArr = [];

    for (const key in glyphs) {
        // Glyph property is part of JS, not the object
        if (!glyphs.hasOwnProperty(key))
            continue;

        const glyph = glyphs[key];

        // Don't select glyphs that have no unicode
        if (glyph == null || glyph.unicodes.length === 0)
            continue;

        // If there is a specific list of glyphs to search then only return those
        if (specificGlyphs != null && !glyph.unicodes.some(e => specificGlyphs.some(c => c === e)))
            continue;

        // Don't use glyphs that are empty
        const boundingBox = glyph.getBoundingBox();
        if (boundingBox.x1 === 0 && boundingBox.x2 === 0 && boundingBox.y1 === 0 && boundingBox.y2 === 0)
            continue;

        // Add the glyph to the return array
        glyphArr.push(glyph);
    }

    // Return the sorted glyph set
    return glyphArr
        .sort(function (glyphA, glyphB) {
            const maxSideA = Math.max((glyphA.xMax - glyphA.xMin), (glyphA.yMax - glyphA.yMin));
            const maxSideB = Math.max((glyphB.xMax - glyphB.xMin), (glyphB.yMax - glyphB.yMin));

            return maxSideA - maxSideB;
        })
        .reverse()
    ;
}

app.ports.onDownload.subscribe(function(canvasId) {
    document.getElementById("downloadLink").setAttribute("href", document.getElementById(canvasId).toDataURL("image/png"));
});

app.ports.sendToScalaJS.subscribe(function(msg) {
  app.ports.receiveFromScalaJS.send(ElmMailbox.post(JSON.stringify(msg)));
});

app.ports.processFont.subscribe(function(fontData) {
    if (fontData.fontPath === null)
        return;

    let specificGlyphs = null;
    if (fontData.renderType === 0)
        specificGlyphs = [...Array(255).keys()];
    else if (fontData.renderType === 1)
        if (fontData.specificGlyphs === null)
            specificGlyphs = []
        else
            specificGlyphs = [...new Set(
                fontData
                    .specificGlyphs
                    .split('')
                    .map(c => c.codePointAt(0))
            )];

    opentype.load(fontData.fontPath, function(e, font) {
        if (e !== null) {
            app.ports.fontProcessErr.send(e.message);
            return;
        }

        drawFontMap(font, fontData.padding, fontData.size, specificGlyphs);
    });
})