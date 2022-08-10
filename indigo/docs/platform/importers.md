---
id: importers
title: File Format Importers
---

At the time of writing, Indigo has limited support for importing data from Aseprite and Tiled. The support that has been added was built on an "as needed" basis, and is far from complete.

> This is an area of Indigo where contributions are particularly welcome!

_Incidentally, we have no affiliation with the tools below, we just like them. They're affordable and they each do their specific job really well._

The examples of useage below are all from ["The Cursed Pirate"](https://github.com/PurpleKingdomGames/indigo-examples/tree/master/demos/pirate) demo, which you can [play here!](http://localhost:3000/pirate.html)

## Aseprite

[Aseprite](https://www.aseprite.org/) is a absolutely delightful tool for creating pixel art graphics and animations.

### Exporting from Aseprite

Graphics can be exported as any web compatible file format. One thing to be aware of is that Aseprite will allow you to export images that have their pixels magnified. We advise you not to do that for Indigo since the engine will do the magnification for you.

Animations can also be exported as a spritesheet with accompanying JSON data, here is an example of the export window.

**PLEASE NOTE:** The export is done as an "array", Indigo does not support the other "hash" option.

![Aseprite export dialogue window](/img/aseprite_export.png "Aseprite export dialogue window")

The process for loading an animation can be seen in action [here](https://github.com/PurpleKingdomGames/indigo-examples/blob/master/demos/pirate/src/main/scala/pirate/core/InitialLoad.scala#L96), and follows these steps:

1. Export the spritesheet and json data from Aseprite.
2. Statically load the exported spritesheet and json files.
3. Use the `Json.asepriteFromJson(json)` function to read the data.
4. Then use the data to generate a sprite and an animation (`aseprite.toSpriteAndAnimations(dice, assetName)`) supplying the of the spritesheet.
5. Use the sprite in your game

## Tiled

[Tiled](https://www.mapeditor.org/) is an excellent tool that can be used to design and layout pretty much any kind of 2D level you can dream up.

Unfortunately Indigo's support for Tiled is particularly rudimentary, and for now only covers a single layer with a single tileset using static graphics.

Otherwise the process is along the lines of:

1. Load your tile set images into Tiled and design your level.
2. Export the json description of your level from Tiled.
3. Load the tileset image and level JSON into Indigo as static assets.
4. Use the `Json.tiledMapFromJson(json)` function to read the data to make a tile map.
5. Then use the data to create a renderable group using `tileMap.toGroup(Assets.Static.terrainRef)`.
6. And produce also to make a "grid" version of the level for you game's model with `tileMap.toGrid(tileMapper)`.
7. Use the grid in your game for things like collision detection, and the group in your view to draw the level.

Tiled can be see in use here, in The Cursed Pirate demo's [initial loader](https://github.com/PurpleKingdomGames/indigo-examples/blob/master/demos/pirate/src/main/scala/pirate/core/InitialLoad.scala#L73).
