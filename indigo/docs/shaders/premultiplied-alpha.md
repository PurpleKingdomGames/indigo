---
id: premultiplied-alpha
title: Premultiplied Alpha
---

A small but essential piece of information to know when writing shaders in Indigo, is that Indigo uses premultiplied alpha everywhere.

It is beyond the scope of these docs to explain straight vs premultiplied alpha, and it's very easy to search for. However, we can look at a practical example.

What color does this represent?

```glsl
vec4 color = vec4(1.0, 0.0, 0.0, 0.5);
```

The expected visible color here is of course full red that is 50% transparent, and under "straight" alpha, that's exactly what you'd get. In Indigo, the result is actually just 100% red. What is going on?

Well here, the alpha has _not yet been multiplied_ across the color values. To get the desired output we need to do the following:

```glsl
vec4 color = vec4(0.5, 0.0, 0.0, 0.5);
```

...because the amount of red you see in a half transparent red is half the amount of red. Or quite often you see this sort of thing:

```glsl
vec4 color = vec4(1.0, 0.0, 0.0, 0.5);
color = vec4(color.rgb * color.a, color.a);
```

People find it easier to think in straight alpha, but in reality, the images and textures we load are _all_ premultiplied. When you look at a semi-transparent image in an image editor, you're looking at the premultiplied version.

Once you get used to the idea, it makes programming shaders easier.
