---
id: constants
title: Shader Constants, Variables, and Outputs
---

Below are tables of shader constants and variables that are available to you, provided by Indigo, when writing shaders for the WebGL 2.0 renderer.

To clarify terminology use on this page:

1. "Constants" are fixed values that never change.
1. "Variables" are values that you should treat as read only, and that will change between program runs.
1. "Outputs" are values that you may be able to read, and may wish to write to in order to affect the final rendering.

## Common

These values are available to all shader programs.

### Constants

Name | Type | Description
---|---|---
`PI`|`float`|`3.141592653589793` or 180 degrees.
`PI_2`|`float`|`PI / 2` or 90 degrees.
`PI_4`|`float`|`PI / 4` or 45 degrees.
`TAU`|`float`|`2.0 * PI` or 360 degrees.
`TAU_2`|`float`|`PI` or 180 degrees.
`TAU_4`|`float`|`PI / 2` or 90 degrees.
`TAU_8`|`float`|`PI / 4` or 45 degrees.

### Variables (read only)

Name | Type | Description
---|---|---
`TIME`|`float`|Running time in seconds.
`VIEWPORT_SIZE`|`vec2`|Size of the viewport in pixels.

## Vertex Shader Programs

> Note that `SIZE` and `UV` are treated differently between entity and blend shaders, this may be revised in the future.

### Common

#### Outputs (read/write)

Name | Type | Description
---|---|---
`VERTEX`|`vec4`|The output position of the current vertex.

### Entity Shader Specific

#### Variables (read only)

Name | Type | Description
---|---|---
`ATLAS_SIZE`|`vec2`|Size of the atlas this texture is on, in pixels.
`CHANNEL_0_POSITION`|`vec2`|Top left position of this texture on the atlas in UV coords.
`CHANNEL_0_SIZE`|`vec2`|Size of this texture on the atlas in UV coords.
`FRAME_SIZE`|`vec2`|Size of sub-rectangle of the texture to be rendered, e.g. the crop (graphic) or animation frame (sprite).
`SIZE`|`vec2`|The width and height in pixels of the space on the screen being drawn to, e.g. the width and height of a graphic.
`TEXTURE_SIZE`|`vec2`|Size of the texture in pixels.
`UV`|`vec2`|The interpolates UV coordinates that will be passed to the fragment shader. Ranges from (0,0) top left to (1,1) bottom right. (Equivalent to `TEXTURE_COORDS`, but read only.)

#### Outputs (read/write)

Name | Type | Description
---|---|---
`CHANNEL_0_ATLAS_OFFSET`|`vec2`|The position on the texture atlas for the texture assigned to channel 0.
`CHANNEL_1_ATLAS_OFFSET`|`vec2`|The position on the texture atlas for the texture assigned to channel 1.
`CHANNEL_2_ATLAS_OFFSET`|`vec2`|The position on the texture atlas for the texture assigned to channel 2.
`CHANNEL_3_ATLAS_OFFSET`|`vec2`|The position on the texture atlas for the texture assigned to channel 3.
`CHANNEL_0_TEXTURE_COORDS`|`vec2`|The scaled, interpolated texture coordinates for the texture in channel 0, uses `CHANNEL_0_ATLAS_OFFSET`.
`CHANNEL_1_TEXTURE_COORDS`|`vec2`|The scaled, interpolated texture coordinates for the texture in channel 1, uses `CHANNEL_1_ATLAS_OFFSET`.
`CHANNEL_2_TEXTURE_COORDS`|`vec2`|The scaled, interpolated texture coordinates for the texture in channel 2, uses `CHANNEL_2_ATLAS_OFFSET`.
`CHANNEL_3_TEXTURE_COORDS`|`vec2`|The scaled, interpolated texture coordinates for the texture in channel 3, uses `CHANNEL_3_ATLAS_OFFSET`.
`ROTATION`|`float`|The rotation amount in radians to be applied to the current vertex.
`TEXTURE_COORDS` (Redundant, use `UV`)|`vec2`|The interpolates UV coordinates that will be passed to the fragment shader. Ranges from (0,0) top left to (1,1) bottom right. (Equivalent to `UV`, but modifiable.)

### Blend Shader Specific

#### Outputs (read/write)

Name | Type | Description
---|---|---
`SIZE`|`vec2`|The width and height in pixels of the space on the screen being drawn to, e.g. the width and height of a graphic.
`UV`|`vec2`|The interpolates UV coordinates. Ranges from (0,0) top left to (1,1) bottom right.

## Fragment Shader Programs

### Common

#### Variables (read only)

Name | Type | Description
---|---|---
`SIZE`|`vec2`|The width and height in pixels of the space on the screen being drawn to, e.g. the width and height of a graphic.
`UV`|`vec2`|The interpolates UV coordinates. Ranges from (0,0) top left to (1,1) bottom right.

#### Outputs (read/write)

Name | Type | Description
---|---|---
`COLOR`|`vec4`|Final pixel color.

### Entity Shader Specific

#### Variables (read only)

Name | Type | Description
---|---|---
`ATLAS_SIZE`|`vec2`|Size of the atlas this texture is on, in pixels.
`CHANNEL_0`|`vec4`|Pixel color value from texture channel 0.
`CHANNEL_1`|`vec4`|Pixel color value from texture channel 1.
`CHANNEL_2`|`vec4`|Pixel color value from texture channel 2.
`CHANNEL_3`|`vec4`|Pixel color value from texture channel 3.
`CHANNEL_0_POSITION`|`vec2`|Top left position of this texture on the atlas in UV coords.
`CHANNEL_0_SIZE`|`vec2`|Size of this texture on the atlas in UV coords.
`CHANNEL_0_TEXTURE_COORDS`|`vec2`|Scaled texture coordinates.
`CHANNEL_1_TEXTURE_COORDS`|`vec2`|Scaled texture coordinates.
`CHANNEL_2_TEXTURE_COORDS`|`vec2`|Scaled texture coordinates.
`CHANNEL_3_TEXTURE_COORDS`|`vec2`|Scaled texture coordinates.
`LIGHT_COUNT`|`int`|Total number of lights in the scene.
`LIGHT_INDEX`|`int`|Index of the current light starting at 0 with a range of 0 to 7.
`ROTATION`|`float`|The rotation amount in radians.
`SCREEN_COORDS`|`vec2`|The absolute position of this pixel on the screen.
`SRC_CHANNEL`|`sampler2D`|The texture channel for the source image data. Current we only bind one atlas at a time. The 4 channels otherwise mention are all textures on the same atlas. It's a limitation we may remove in the future.
`TEXTURE_SIZE`|`vec2`|Size of the texture in pixels.

#### Outputs (read/write)

> Please note that `LIGHT_` variables change during execution of the program to show the details of the current light affecting the element being drawn.

Name | Type | Description
---|---|---
`LIGHT_ACTIVE`|`int`|Is the light enabled?
`LIGHT_ANGLE`|`float`|Used in spotlights to determine the angle of the light cone.
`LIGHT_COLOR`|`vec4`|The color of the light.
`LIGHT_FALLOFF_TYPE`|`int`|The type of falloff to be used: 0 = none, 1 = smooth linear, 2 = smooth quadtratic, 3 = linear, 4 = quadratic
`LIGHT_FAR`|`float`|The maximum distance from the light up to which it will affect the scene.
`LIGHT_FAR_CUT_OFF`|`int`|Values are 1 or 0, this is really a boolean that determines if the `LIGHT_FAR` value is used or not. If not the light will attenuate forever.
`LIGHT_INTENSITY`|`float`|The intensity of the light.
`LIGHT_NEAR`|`float`|The minimum distance to the light where it will begin to affect the scene.
`LIGHT_POSITION`|`vec2`|The light's position.
`LIGHT_ROTATION`|`float`|The light's rotation.
`LIGHT_SPECULAR`|`vec4`|The specular color of the light.
`LIGHT_TYPE`|`int`|What type of light is it? (0 = ambient, 1 = direction, 2 = point, 3 = spot)

### Blend Shader Specific

#### Variables (read only)

Name | Type | Description
---|---|---
`DST_CHANNEL`|`sampler2D`|The texture channel for the destination image data.
`SRC_CHANNEL`|`sampler2D`|The texture channel for the source image data.

#### Outputs (read/write)

Name | Type | Description
---|---|---
`DST`|`vec4`|Pixel color value from DST texture.
`SRC`|`vec4`|Pixel color value from SRC texture.
