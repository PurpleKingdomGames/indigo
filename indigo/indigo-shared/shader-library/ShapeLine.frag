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
  vec2 START;
  vec2 END;
};

// Borrowed with thanks! https://www.iquilezles.org/www/articles/distfunctions2d/distfunctions2d.htm
float sdfCalc(vec2 p, vec2 a, vec2 b) {
  vec2 pa = p - a;
  vec2 ba = b - a;
  float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
  return length(pa - ba * h);
}

void fragment() {

  float strokeWidthHalf = max(0.0, STROKE_WIDTH / SIZE.x / 2.0);

  float sdf = sdfCalc(UV, START / SIZE, END / SIZE);
  float strokeSdf = sdf - strokeWidthHalf;

  float strokeAmount = (1.0 - step(0.0, strokeSdf)) * STROKE_COLOR.a;

  vec4 strokeColor = vec4(STROKE_COLOR.rgb * strokeAmount, strokeAmount);

  COLOR = strokeColor;

}
//</indigo-fragment>
