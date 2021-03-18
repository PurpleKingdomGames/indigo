#version 300 es

precision mediump float;

uniform sampler2D DST_CHANNEL;

vec2 UV;
vec2 SIZE;
vec4 SRC;
vec4 COLOR;

//<indigo-fragment>
layout (std140) uniform IndigoRefractionBlendData {
  float REFRACTION_AMOUNT;
};

void fragment(){
  vec2 normal = normalize((2.0 * SRC) - 1.0).rg;
  vec2 offset =  UV + ((1.0 / SIZE) * normal * REFRACTION_AMOUNT);

  COLOR = texture(DST_CHANNEL, offset);
}
//</indigo-fragment>
