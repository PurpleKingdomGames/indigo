#version 300 es

precision mediump float;

// Uniforms
uniform sampler2D u_texture;
uniform vec4 u_tint;

// Varying
in vec2 v_texcoord;
out vec4 fragColor;

void main(void) {
  vec4 textureColor = texture(u_texture, v_texcoord);

  float average = (textureColor.r + textureColor.g + textureColor.b) / float(3);

  fragColor = vec4(textureColor.rgb * u_tint.rgb, average * u_tint.a);
}
