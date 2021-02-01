#version 300 es

precision lowp float;

uniform sampler2D u_texture;

in vec2 v_texcoord;
in vec4 v_tint;
in float v_alpha;

out vec4 fragColor;

void main(void) {
  vec4 textureColor = texture(u_texture, v_texcoord);

  vec4 withAlpha = vec4(textureColor.rgb, textureColor.a * v_alpha);

  vec4 tintedVersion = vec4(withAlpha.rgb * v_tint.rgb, withAlpha.a);

  fragColor = mix(withAlpha, tintedVersion, max(0.0, v_tint.a));
}
