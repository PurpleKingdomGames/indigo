#version 300 es

precision mediump float;

vec2 UV;
vec4 COLOR;
float TAU;
float TIME;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform CustomData {
  float BORDER_WIDTH;
  float SMOOTH;
  vec4 BORDER_COLOR;
  vec4 FILL_COLOR;
};

float timeToRadians(float t) {
  return TAU * mod(t, 1.0);
}

void fragment() {

  float padding = 1.0 / SIZE.x;

  float borderWidthPx = BORDER_WIDTH;
  float borderWidth = borderWidthPx / SIZE.x; // circle, so equal w/h

  vec4 borderColor = vec4(BORDER_COLOR.rgb * BORDER_COLOR.a, BORDER_COLOR.a);
  vec4 fillColor = vec4(FILL_COLOR.rgb * FILL_COLOR.a, FILL_COLOR.a);

  float borderSdf = length(UV - 0.5) - (0.5 - padding);
  float borderAmount = 1.0 - step(0.0, borderSdf);

  float fillSdf = length(UV - 0.5) - (0.5 - (borderWidth + padding));
  float fillAmount = 1.0 - step(0.0, fillSdf);

  if(SMOOTH > 0.0) {
    float borderAA = cos(borderSdf * SIZE.x) * (1.0 - clamp(abs(borderSdf * SIZE.x), 0.0, 1.0));
    borderAmount = max(borderAmount, borderAA);

    float fillAA = cos(fillSdf * SIZE.x) * (1.0 - clamp(abs(fillSdf * SIZE.x), 0.0, 1.0));
    fillAmount = max(fillAmount, fillAA);
  }

  vec4 paintColor = mix(borderColor, fillColor, fillAmount);
  float paintAmount = 0.0;

  if(fillSdf > 0.0) {
    paintAmount = borderAmount * BORDER_COLOR.a;
  } else {
    paintAmount = fillAmount * FILL_COLOR.a;
  }

  vec4 circle = vec4(paintColor.rgb * paintAmount, paintColor.a * paintAmount);
  
  COLOR = circle;
}
//</indigo-fragment>
