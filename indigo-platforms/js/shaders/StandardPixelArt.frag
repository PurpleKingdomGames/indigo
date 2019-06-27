#version 300 es

precision mediump float;

// Uniforms
uniform sampler2D u_texture;

// Varying
in vec2 v_texcoord;
in vec4 v_tint;

out vec4 fragColor;

void main(void) {
  vec4 textureColor = texture(u_texture, v_texcoord);
  fragColor = textureColor * v_tint;
}
