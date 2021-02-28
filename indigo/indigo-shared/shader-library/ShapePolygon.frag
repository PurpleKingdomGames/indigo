#version 300 es

precision mediump float;

vec2 UV;
vec4 COLOR;
float TAU;
float TIME;
vec2 SIZE;

//<indigo-fragment>
const int MAX_VERTICES = 16;

layout (std140) uniform CustomData {
  float STROKE_WIDTH;
  float COUNT;
  vec4 STROKE_COLOR;
  vec4 FILL_COLOR;
  vec2[MAX_VERTICES] VERTICES;
};

// Borrowed with thanks! https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
float sdfCalc(vec2 p, int count, vec2[MAX_VERTICES] v) {
    float d = dot(p - v[0], p - v[0]);
    float s = 1.0;

    for(int i = 0, j = count - 1; i < count; j = i, i++) {
        vec2 e = v[j] - v[i];
        vec2 w =    p - v[i];
        vec2 b = w - e * clamp(dot(w, e) / dot(e, e), 0.0, 1.0);
        d = min(d, dot(b, b));
        bvec3 c = bvec3(p.y >= v[i].y, p.y < v[j].y, e.x * w.y > e.y * w.x);
        if(all(c) || all(not(c))) s *= -1.0;  
    }

    return s * sqrt(d);
}

vec2[MAX_VERTICES] toUvSpace(int count, vec2[MAX_VERTICES] v) {
  vec2[MAX_VERTICES] polygon;
  
  for(int i = 0; i < count; i++) {
    polygon[i] = v[i] / SIZE;
  }

  return polygon;
}

void fragment() {

  float strokeWidthHalf = max(0.0, STROKE_WIDTH / SIZE.x / 2.0);

  int iCount = int(COUNT);

  vec2[MAX_VERTICES] polygon = toUvSpace(iCount, VERTICES);

  float sdf = sdfCalc(UV, iCount, polygon);
  float annularSdf = abs(sdf) - strokeWidthHalf;

  float fillAmount = (1.0 - step(0.0, sdf)) * FILL_COLOR.a;
  float strokeAmount = (1.0 - step(0.0, annularSdf)) * STROKE_COLOR.a;

  vec4 fillColor = vec4(FILL_COLOR.rgb * fillAmount, fillAmount);
  vec4 strokeColor = vec4(STROKE_COLOR.rgb * strokeAmount, strokeAmount);

  COLOR = mix(fillColor, strokeColor, strokeAmount);

}
//</indigo-fragment>
