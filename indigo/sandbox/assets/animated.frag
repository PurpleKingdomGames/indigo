#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;

vec4 COLOR;
vec2 UV;
vec2 TEXTURE_SIZE;
vec2 SIZE;
vec2 CHANNEL_0_POSITION;
vec2 CHANNEL_0_SIZE;

//<indigo-fragment>
layout (std140) uniform AnimatedGraphic {
  highp float FRAME_COUNT;
  vec4[8] FRAMES;
  float[8] DURATIONS;
};

void fragment(){
  vec2 tiledUVs = CHANNEL_0_POSITION + (fract(UV * (SIZE / TEXTURE_SIZE)) * CHANNEL_0_SIZE);

  COLOR = texture(SRC_CHANNEL, tiledUVs);
}
//</indigo-fragment>
