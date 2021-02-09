#version 300 es

precision mediump float;

vec4 CHANNEL_0;
vec4 COLOR;

//<indigo-fragment>
layout (std140) uniform CustomData {
  float ALPHA;
};

void fragment(){
  COLOR = vec4(CHANNEL_0.rgb, CHANNEL_0.a * ALPHA);
}
//</indigo-fragment>

//<indigo-light>
void light(){}
//</indigo-light>
