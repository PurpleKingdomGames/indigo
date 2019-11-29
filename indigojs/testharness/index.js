
'use strict';

const config =
    GameConfigOps.default
        .withViewport(new GameViewport(200, 100))
        .withClearColor(new ClearColor(1, 0, 1, 1))
        .withFrameRate(30)
        .withMagnification(2);

const assets = [];

const fonts = [];

const animations = [];

Indigo.init(
    config,
    assets,
    fonts,
    animations
);
