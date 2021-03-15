#version 300 es

precision mediump float;

vec4 SRC;
vec4 COLOR;

//<indigo-fragment>
layout (std140) uniform IndigoLightingBlendData {
  vec4 AMBIENT_LIGHT_COLOR;
};

void fragment(){
  vec4 ambient = vec4(AMBIENT_LIGHT_COLOR.rgb * AMBIENT_LIGHT_COLOR.a, 1.0);

  COLOR = ambient + SRC;
}
//</indigo-fragment>
