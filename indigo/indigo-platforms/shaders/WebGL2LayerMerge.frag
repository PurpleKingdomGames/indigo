#version 300 es

precision lowp float;

in vec2 v_texcoord;
in vec2 v_size;

uniform sampler2D u_texture_layer;

out vec4 fragColor;

void main(void) {
  fragColor = texture(u_texture_layer, v_texcoord);
}
