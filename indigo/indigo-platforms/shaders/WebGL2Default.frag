#version 300 es

precision lowp float;

uniform sampler2D u_texture;

in vec2 v_texcoords;

layout(location = 0) out vec4 fragColor;

void main(void) {
  fragColor = texture(u_texture, v_texcoords);
}
