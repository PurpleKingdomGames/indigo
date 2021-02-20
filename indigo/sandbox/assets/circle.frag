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
  vec3 BORDER_COLOR;
  vec3 FILL_COLOR;
};


float timeToRadians(float t) {
  return TAU * mod(t, 1.0);
}

void fragment() {

  float borderWidthPx = BORDER_WIDTH;//4.0;
  float borderWidth = -(borderWidthPx / SIZE.x); // circle, so equal w/h

  vec3 fillColor = FILL_COLOR;//vec3(0.0, 0.0, 0.0);
  vec3 borderColor = BORDER_COLOR;//vec3(1.0, 1.0, 1.0);

  float sdf = length(UV - 0.5) - 0.5;
  float fillAmount = 1.0 - step(borderWidth, sdf);
  float borderAmount = 1.0 - step(0.0, sdf);

  vec3 paintColor = mix(borderColor, fillColor, fillAmount);
  float paintAmount = max(fillAmount, borderAmount) * ALPHA;
  vec4 circle = vec4(paintColor * paintAmount, paintAmount);
  
  COLOR = circle;
}
//</indigo-fragment>
