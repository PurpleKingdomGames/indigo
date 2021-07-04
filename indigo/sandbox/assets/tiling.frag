#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;

vec4 COLOR;
vec2 UV;
vec2 TEXTURE_SIZE;
vec2 SIZE;
vec2 CHANNEL_0_POSITION_ON_ATLAS;
vec2 CHANNEL_0_SIZE_ON_ATLAS;

//<indigo-fragment>
void fragment(){
  vec2 tiledUVs = CHANNEL_0_POSITION_ON_ATLAS + (fract(UV * (SIZE / TEXTURE_SIZE)) * CHANNEL_0_SIZE_ON_ATLAS);

  COLOR = texture(SRC_CHANNEL, tiledUVs);
}
//</indigo-fragment>
