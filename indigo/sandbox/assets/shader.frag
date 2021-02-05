#version 300 es

precision mediump float;

vec2 UV;
vec4 COLOR;

//<indigo-fragment>

layout (std140) uniform CustomData {
  // vec4 ALPHA_BORDER_COLOR;
  float ALPHA;
  vec4 BORDER_COLOR;
};

float sdf(vec2 p) {
  float b = 0.45;
  vec2 d = abs(p) - b;
  float dist = length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);
  return step(0.0, dist);
}

void fragment(){
  // float ALPHA = ALPHA_BORDER_COLOR.x;
  // vec3 BORDER_COLOR = ALPHA_BORDER_COLOR.yzw;

  float amount = sdf(UV - 0.5);
  COLOR=vec4(BORDER_COLOR.rgb, amount * ALPHA);
}
//</indigo-fragment>

//<indigo-light>
void light(){}
//</indigo-light>
