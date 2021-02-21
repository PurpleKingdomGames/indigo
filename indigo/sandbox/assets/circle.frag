#version 300 es

precision mediump float;

vec2 UV;
vec4 COLOR;
float TAU;
float TIME;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform CustomData {
  float STROKE_WIDTH;
  float SMOOTH;
  vec4 STROKE_COLOR;
  vec4 FILL_COLOR;
};

float timeToRadians(float t) {
  return TAU * mod(t, 1.0);
}

void fragment() {

  float padding = 1.0 / SIZE.x;

  float strokeWidthPx = STROKE_WIDTH;
  float strokeWidth = strokeWidthPx / SIZE.x; // circle, so equal w/h

  vec4 strokeColor = vec4(STROKE_COLOR.rgb * STROKE_COLOR.a, STROKE_COLOR.a);
  vec4 fillColor = vec4(FILL_COLOR.rgb * FILL_COLOR.a, FILL_COLOR.a);

  float strokeSdf = length(UV - 0.5) - (0.5 - padding);
  float strokeAmount = 1.0 - step(0.0, strokeSdf);

  float fillSdf = length(UV - 0.5) - (0.5 - (strokeWidth + padding));
  float fillAmount = 1.0 - step(0.0, fillSdf);

  if(SMOOTH > 0.0) {
    float strokeAA = cos(strokeSdf * SIZE.x) * (1.0 - clamp(abs(strokeSdf * SIZE.x), 0.0, 1.0));
    strokeAmount = max(strokeAmount, strokeAA);

    float fillAA = cos(fillSdf * SIZE.x) * (1.0 - clamp(abs(fillSdf * SIZE.x), 0.0, 1.0));
    fillAmount = max(fillAmount, fillAA);
  }

  vec4 paintColor = mix(strokeColor, fillColor, fillAmount);
  float paintAmount = 0.0;

  if(fillSdf > 0.0) {
    paintAmount = strokeAmount * STROKE_COLOR.a;
  } else {
    paintAmount = fillAmount * FILL_COLOR.a;
  }

  vec4 circle = vec4(paintColor.rgb * paintAmount, paintColor.a * paintAmount);
  
  COLOR = circle;
}
//</indigo-fragment>
