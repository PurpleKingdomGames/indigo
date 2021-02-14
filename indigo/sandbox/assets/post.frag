#version 300 es

precision mediump float;

float TAU;
float TIME;
vec4 COLOR;

//<indigo-post-fragment>
float timeToRadians(float t) {
  return TAU * mod(t, 1.0);
}

void postFragment() {
  float amount = timeToRadians(TIME / 2.0) / TAU;
  COLOR = vec4(COLOR.rgb * amount, COLOR.a);
}
//</indigo-post-fragment>
