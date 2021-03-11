#version 300 es

precision mediump float;

vec4 SRC;
vec4 COLOR;

//<indigo-fragment>
void fragment(){
  COLOR = SRC;
}
//</indigo-fragment>
