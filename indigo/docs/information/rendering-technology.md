---
id: rendering-technology
title: Rendering Technology
---

At the time of writing, there are five options for rendering 2D graphics into a browser page, if you are so inclined:

1. HTML + CSS + SVG
2. 2D Canvas page element
3. WebGL 1.0
4. WebGL 2.0
5. WebGPU (not yet released)

## Indigo uses WebGL 2.0

Indigo's primary rendering technology is WebGL 2.0, the most modern 3D rendering api available to the majority of browsers.

> Safari notably doesn't support WebGL 2.0 by default yet, but it is in beta. 13/04/2021

By default, Indigo will attempt to detect whether WebGL 2.0 is supported, and if not, will fall back to a WebGL 1.0 renderer. You can also force it to use one or the other.

The WebGL 1.0 renderer has been reduced to the bare minimum so that all maintenance efforts can go into the WebGL 2.0 version. The anticipated use of this renderer is to act as a simple fall back, so you can message your players that there has been some sort of problem rendering with WebGL 2.0. That said, the WebGL 1.0 renderer does work and being so simple is very fast! You could use it as your primary renderer if you don't need flashy graphics!

Renderer detection can be done by listening for the `RendererDetails` event.

## Future implementations

Indigo can theoretically support any number of renderer implementations as the scene description is well separated from the rendering implementation, we just happen to have WebGL 1.0 and 2.0 support at the moment.

Some browser manufacturers seem to be holding out for WebGPU, but it's very much [a work in progress](https://github.com/gpuweb/gpuweb/wiki/Implementation-Status). We intend to support it sooner or later if possible.

There are no plans to support an [HTML or Canvas based renderers](http://buildnewgames.com/dom-sprites/) at the moment, but there is no [technical reason why we couldn't](http://buildnewgames.com/assets/article//dom-sprites/dom-sprite-demo.html).
