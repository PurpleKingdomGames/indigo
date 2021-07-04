#version 300 es

precision mediump float;


uniform sampler2D SRC_CHANNEL;

// vec4 CHANNEL_0;
vec4 COLOR;
vec2 UV;
vec2 TEXTURE_SIZE;
vec2 SIZE;
vec2 CHANNEL_0_ATLAS_POSITION;
vec2 CHANNEL_0_SIZE_ON_ATLAS;
vec2 CHANNEL_0_TEXTURE_COORDS;

//<indigo-fragment>
void fragment(){
  vec2 GRID_DIMENSIONS = ceil(SIZE / TEXTURE_SIZE);
  vec2 relUV = CHANNEL_0_ATLAS_POSITION + (fract(UV * GRID_DIMENSIONS) * CHANNEL_0_SIZE_ON_ATLAS);

  COLOR = texture(SRC_CHANNEL, relUV);
}
//</indigo-fragment>
