#version 300 es

precision mediump float;

vec2 POSITION;
vec2 SCALE;

//<indigo-vertex>
layout (std140) uniform MutantData {
  vec2 MOVE_TO;
  vec2 SCALE_TO;
  float ALPHA;
};

vec4 vertex(vec4 v){
  POSITION = MOVE_TO;
  SCALE = SCALE_TO;
  return v;
}
//</indigo-vertex>
