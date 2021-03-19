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
  vec2 normal = normalize(SRC - 0.5).rg;
  vec2 offset = UV + (normal * REFRACTION_AMOUNT * SRC.a);

  COLOR = texture(DST_CHANNEL, offset);
}
//</indigo-fragment>
