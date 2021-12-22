#version 300 es

precision mediump float;

vec4 COLOR;
vec4 CHANNEL_0;

//<indigo-fragment>
void fragment(){
  COLOR = CHANNEL_0;
}
//</indigo-fragment>
