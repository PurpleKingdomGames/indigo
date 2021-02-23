#version 300 es

precision mediump float;

vec2 UV;
vec4 COLOR;
float TAU;
float TIME;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform CustomData {
  vec2 ASPECT_RATIO;
  float STROKE_WIDTH;
  vec4 STROKE_COLOR;
  vec4 FILL_COLOR;
};

// Borrowed with thanks! https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
float sdfCalc(vec2 p, vec2 b){
  vec2 d = abs(p) - b;
  return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
}

void fragment() {

  float strokeWidthHalf = max(0.0, STROKE_WIDTH / SIZE.x / 2.0); // TODO... 

  float sdf = sdfCalc(UV - 0.5, (vec2(0.5) * ASPECT_RATIO) - strokeWidthHalf);
  float annularSdf = abs(sdf) - strokeWidthHalf;

  float fillAmount = (1.0 - step(0.0, sdf)) * FILL_COLOR.a;
  float strokeAmount = (1.0 - step(0.0, annularSdf)) * STROKE_COLOR.a;

  vec4 fillColor = vec4(FILL_COLOR.rgb * fillAmount, fillAmount);
  vec4 strokeColor = vec4(STROKE_COLOR.rgb * strokeAmount, strokeAmount);

  COLOR = mix(fillColor, strokeColor, strokeAmount);

}
//</indigo-fragment>
