#version 300 es

precision mediump float;

vec2 UV;
vec4 COLOR;
float TAU;
float TIME;

//<indigo-fragment>
float timeToRadians(float t) {
  return TAU * mod(t, 1.0);
}



void fragment() {

  float sdf = length(UV - 0.5) - 0.5;
  float alpha = 1.0 - step(0.0, sdf);

  vec4 circle = vec4(vec3(0.0, 1.0, 0.0) * alpha, alpha);
  
  COLOR = circle;
}
//</indigo-fragment>

//<indigo-light>
void light(){}
//</indigo-light>
