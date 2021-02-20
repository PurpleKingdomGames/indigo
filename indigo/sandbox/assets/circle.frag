#version 300 es

precision mediump float;

vec2 UV;
vec4 COLOR;
float TAU;
float TIME;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform CustomData {
  float ALPHA;
  float BORDER_WIDTH;
  float SMOOTH;
  vec3 BORDER_COLOR;
  vec3 FILL_COLOR;
};


float timeToRadians(float t) {
  return TAU * mod(t, 1.0);
}

void fragment() {

  float borderWidthPx = BORDER_WIDTH;
  float borderWidth = borderWidthPx / SIZE.x; // circle, so equal w/h

  vec3 fillColor = FILL_COLOR;
  vec3 borderColor = BORDER_COLOR;

  float borderSdf = length(UV - 0.5) - 0.5;
  float borderAmount = 1.0 - step(0.0, borderSdf);

  float fillSdf = length(UV - 0.5) - (0.5 - borderWidth);
  float fillAmount = 1.0 - step(0.0, fillSdf);

  if(SMOOTH > 0.0) {
    float borderAA = cos(borderSdf * SIZE.x) * (1.0 - clamp(abs(borderSdf * SIZE.x), 0.0, 1.0));
    borderAmount = max(borderAmount, borderAA);

    float fillAA = cos(fillSdf * SIZE.x) * (1.0 - clamp(abs(fillSdf * SIZE.x), 0.0, 1.0));
    fillAmount = max(fillAmount, fillAA);
  }

  vec3 paintColor = mix(borderColor, fillColor, fillAmount);
  float paintAmount = max(fillAmount, borderAmount) * ALPHA;
  vec4 circle = vec4(paintColor * paintAmount, paintAmount);
  
  COLOR = circle;
}
//</indigo-fragment>
