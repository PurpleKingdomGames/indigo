#version 300 es

precision mediump float;

vec2 UV;
vec4 COLOR;
float TAU;
float TIME;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform IndigoShapeData {
  float STROKE_WIDTH;
  float FILL_TYPE;
  vec4 STROKE_COLOR;
  vec4 GRADIENT_FROM_TO;
  vec4 GRADIENT_FROM_COLOR;
  vec4 GRADIENT_TO_COLOR;
};

// Borrowed with thanks! https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
float sdfCalc(vec2 p, float r) {
  return length(p) - r;
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

  float strokeWidthHalf = max(0.0, STROKE_WIDTH / SIZE.x / 2.0); // circle, so equal w/h

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

  float sdf = sdfCalc(UV - 0.5, 0.5 - strokeWidthHalf);
  float annularSdf = abs(sdf) - strokeWidthHalf;

  float fillAmount = (1.0 - step(0.0, sdf)) * fill.a;
  float strokeAmount = (1.0 - step(0.0, annularSdf)) * STROKE_COLOR.a;

  vec4 fillColor = vec4(fill.rgb * fillAmount, fillAmount);
  vec4 strokeColor = vec4(STROKE_COLOR.rgb * strokeAmount, strokeAmount);

  COLOR = mix(fillColor, strokeColor, strokeAmount);

}
//</indigo-fragment>
