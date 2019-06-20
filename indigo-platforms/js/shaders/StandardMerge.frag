#version 300 es

precision mediump float;

// Passed in from the vertex shader.
in vec2 v_texcoord;

// The textures.
uniform sampler2D u_texture_game;
uniform sampler2D u_texture_lighting;
uniform sampler2D u_texture_ui;
uniform vec4 u_tint;

out vec4 fragColor;

void main(void) {
  //
  vec4 textureColorGame = texture(u_texture_game, v_texcoord);
  vec4 textureColorLighting = texture(u_texture_lighting, v_texcoord);
  vec4 textureColorUi = texture(u_texture_ui, v_texcoord);

  vec4 gameAndLighting = textureColorGame * textureColorLighting;

  fragColor = mix(gameAndLighting, textureColorUi, textureColorUi.a);
}
