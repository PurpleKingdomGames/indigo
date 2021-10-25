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

void fragment(){
  float a = CHANNEL_0.a * ALPHA;
  COLOR = vec4(CHANNEL_0.rgb * a, a);
}
//</indigo-fragment>
