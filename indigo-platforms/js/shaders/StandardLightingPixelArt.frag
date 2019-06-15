precision mediump float;

// Passed in from the vertex shader.
varying vec2 v_texcoord;
varying vec4 v_effectValues;

// The texture.
uniform sampler2D u_texture;

void main(void) {
  vec4 textureColor = texture2D(u_texture, v_texcoord);

  float average = (textureColor.r + textureColor.g + textureColor.b) / float(3);

  gl_FragColor =
      vec4(textureColor.rgb * v_effectValues.rgb, average * v_effectValues.a);
}
