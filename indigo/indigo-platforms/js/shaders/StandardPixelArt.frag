#version 300 es

precision mediump float;

uniform sampler2D u_textureDiffuse;
uniform sampler2D u_textureEmission;
uniform sampler2D u_textureNormal;
uniform sampler2D u_textureSpecular;

in vec2 v_texcoord;
in vec4 v_tint;
in float v_alpha;

layout(location = 0) out vec4 fragColor0;
layout(location = 1) out vec4 fragColor1;

void main(void) {
  vec4 textureColor = texture(u_textureDiffuse, v_texcoord);

  vec4 withAlpha = vec4(textureColor.rgb, textureColor.a * v_alpha);

  vec4 tintedVersion = vec4(withAlpha.rgb * v_tint.rgb, withAlpha.a);

  fragColor0 = mix(withAlpha, tintedVersion, max(0.0, v_tint.a));
  fragColor1 = vec4(0.0, 1.0, 0.0, withAlpha.a);
}
