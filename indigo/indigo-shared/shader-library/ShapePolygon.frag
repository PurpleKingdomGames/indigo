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
  float FILL_TYPE;
  float COUNT;
  vec4 STROKE_COLOR;
  vec4 GRADIENT_FROM_TO;
  vec4 GRADIENT_FROM_COLOR;
  vec4 GRADIENT_TO_COLOR;
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

vec4 calculateColor() {
  return vec4(GRADIENT_FROM_COLOR.rgb * GRADIENT_FROM_COLOR.a, GRADIENT_FROM_COLOR.a);
}

vec4 calculateLinearGradient() {
  vec2 pointA = GRADIENT_FROM_TO.xy;
  vec2 pointB = GRADIENT_FROM_TO.zw;
  vec2 pointP = UV * SIZE;

  // `h` is the distance along the gradient 0 at A, 1 at B
  float h = min(1.0, max(0.0, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)));

  vec4 gradient = mix(GRADIENT_FROM_COLOR, GRADIENT_TO_COLOR, h);

  return vec4(gradient.rgb * gradient.a, gradient.a);
}

vec4 calculateRadialGradient() {
  vec2 pointA = GRADIENT_FROM_TO.xy;
  vec2 pointB = GRADIENT_FROM_TO.zw;
  vec2 pointP = UV * SIZE;

  float radius = length(pointB - pointA);
  float distanceToP = length(pointP - pointA);

  float sdf = clamp(-((distanceToP - radius) / radius), 0.0, 1.0);

  vec4 gradient = mix(GRADIENT_TO_COLOR, GRADIENT_FROM_COLOR, sdf);

  return vec4(gradient.rgb * gradient.a, gradient.a);
}

void fragment() {

  float strokeWidthHalf = max(0.0, STROKE_WIDTH / SIZE.x / 2.0);

  int iCount = int(COUNT);

  vec2[MAX_VERTICES] polygon = toUvSpace(iCount, VERTICES);


  //--- Fill
  // 0 = color; 1 = linear gradient; 2 = radial gradient
  int fillType = int(round(FILL_TYPE));
  vec4 fill;

  switch(fillType) {
    case 0:
      fill = calculateColor();
      break;

    case 1:
      fill = calculateLinearGradient();
      break;

    case 2:
      fill = calculateRadialGradient();
      break;

    default:
      fill = calculateColor();
      break;
  }
  //---

  float sdf = sdfCalc(UV, iCount, polygon);
  float annularSdf = abs(sdf) - strokeWidthHalf;

  float fillAmount = (1.0 - step(0.0, sdf)) * fill.a;
  float strokeAmount = (1.0 - step(0.0, annularSdf)) * STROKE_COLOR.a;

  vec4 fillColor = vec4(fill.rgb * fillAmount, fillAmount);
  vec4 strokeColor = vec4(STROKE_COLOR.rgb * strokeAmount, strokeAmount);

  COLOR = mix(fillColor, strokeColor, strokeAmount);

}
//</indigo-fragment>
