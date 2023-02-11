#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;

vec4 CHANNEL_0;
vec4 COLOR;

//<indigo-fragment>
layout (std140) uniform MutantData {
  vec2 MOVE_TO;
  vec2 SCALE_TO;
  float ALPHA;
};

vec4 fragment(vec4 c){
  float a = CHANNEL_0.a * ALPHA;
  return vec4(CHANNEL_0.rgb * a, a);
}
//</indigo-fragment>
