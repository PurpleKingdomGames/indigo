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
  vec4 STROKE_COLOR;
  vec4 FILL_COLOR;
};

float sdfCalc(vec2 p, float r) {
  return length(p) - r;
}

void fragment() {

  float strokeWidthHalf = max(0.0, STROKE_WIDTH / SIZE.x / 2.0); // circle, so equal w/h

  float sdf = sdfCalc(UV - 0.5, 0.5 - strokeWidthHalf);
  float annularSdf = abs(sdf) - strokeWidthHalf;

  float fillAmount = (1.0 - step(0.0, sdf)) * FILL_COLOR.a;
  float strokeAmount = (1.0 - step(0.0, annularSdf)) * STROKE_COLOR.a;

  vec4 fillColor = vec4(FILL_COLOR.rgb * fillAmount, fillAmount);
  vec4 strokeColor = vec4(STROKE_COLOR.rgb * strokeAmount, strokeAmount);

  COLOR = mix(fillColor, strokeColor, strokeAmount);

}
//</indigo-fragment>
