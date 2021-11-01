#version 300 es

precision mediump float;

vec4 SRC;
vec4 COLOR;

//<indigo-fragment>
void fragment() {
  COLOR = vec4(vec3(1.0, 0.0, 0.0) * SRC.a, SRC.a);
}
//</indigo-fragment>
