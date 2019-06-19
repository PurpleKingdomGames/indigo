precision mediump float;

// Uniforms
uniform sampler2D u_texture;
uniform vec4 u_tint;

// Varying
varying vec2 v_texcoord;

void main(void) {
  vec4 textureColor = texture2D(u_texture, v_texcoord);

  float average = (textureColor.r + textureColor.g + textureColor.b) / float(3);

  gl_FragColor = vec4(textureColor.rgb * u_tint.rgb, average * u_tint.a);
}
