float TAU;
float TIME;
vec4 VERTEX;

//<indigo-post-vertex>
float timeToRadians(float t) {
  return TAU * mod(t, 1.0);
}

void postVertex() {
  float orbitDist = 0.5;
  float x = sin(timeToRadians(TIME / 2.0)) * orbitDist + VERTEX.x;
  float y = cos(timeToRadians(TIME / 2.0)) * orbitDist + VERTEX.y;
  vec2 orbit = vec2(x, y);
  VERTEX = vec4(orbit, VERTEX.zw);
}
//</indigo-post-vertex>
