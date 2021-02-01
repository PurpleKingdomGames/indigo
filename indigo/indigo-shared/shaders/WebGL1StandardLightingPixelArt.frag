precision mediump float;

// Uniforms
uniform sampler2D u_texture;
uniform vec4 u_tint;

// Varying
varying vec2 v_texcoord;
varying float v_alpha;

void main(void) {
  vec4 textureColor = texture2D(u_texture, v_texcoord);

  vec4 withAlpha = vec4(textureColor.rgb, textureColor.a * v_alpha);

  vec4 tintedVersion = vec4(withAlpha.rgb * u_tint.rgb, withAlpha.a);

  gl_FragColor = mix(withAlpha, tintedVersion, max(0.0, u_tint.a));
}
