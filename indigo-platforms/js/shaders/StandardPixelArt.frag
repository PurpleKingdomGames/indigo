precision mediump float;

// Uniforms
uniform sampler2D u_texture;
uniform vec4 u_tint;

// Varying
varying vec2 v_texcoord;

void main(void) {
  vec4 textureColor = texture2D(u_texture, v_texcoord);
  gl_FragColor = textureColor * u_tint;
}
