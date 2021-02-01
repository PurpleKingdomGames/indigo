precision mediump float;

// Passed in from the vertex shader.
varying vec2 v_texcoord;

// The textures.
uniform sampler2D u_texture_game;
uniform sampler2D u_texture_lighting;
uniform sampler2D u_texture_ui;
uniform vec4 u_tint;

void main(void) {
  vec4 textureColorGame = texture2D(u_texture_game, v_texcoord);
  vec4 textureColorLighting = texture2D(u_texture_lighting, v_texcoord);
  vec4 textureColorUi = texture2D(u_texture_ui, v_texcoord);

  vec4 gameAndLighting = textureColorGame * textureColorLighting;

  gl_FragColor = mix(gameAndLighting, textureColorUi, textureColorUi.a);
}
