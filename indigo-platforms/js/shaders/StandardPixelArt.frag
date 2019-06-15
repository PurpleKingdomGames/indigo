precision mediump float;

// Passed in from the vertex shader.
varying vec2 v_texcoord;
varying vec4 v_effectValues;

// The texture.
uniform sampler2D u_texture;

void main(void) {
  vec4 textureColor = texture2D(u_texture, v_texcoord);
  gl_FragColor = textureColor * v_effectValues;
}
