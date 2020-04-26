#version 300 es

precision lowp float;

uniform sampler2D u_texture;

in vec2 v_texcoord;
in float v_alpha;

out vec4 fragColor;

void main(void) {
  // Currently expecting ordinary normal maps. Might make it accept height maps in a minute.
  vec4 textureColor = texture(u_texture, v_texcoord);

  // Convert RGB 0 to 1, into -1 to 1
  vec2 normal = normalize((2.0 * textureColor) - 1.0).rg;

  // use Alpha as an amount multiplier.
  vec2 withAlpha = normal * v_alpha;

  // Convert -1 to 1, back into RGB 0 to 1
  vec2 asColorSpace = (withAlpha + 1.0) / 2.0;

  float redBlend = abs(asColorSpace.r - 0.5) * 2.0;
  float greenBlend = abs(asColorSpace.g - 0.5) * 2.0;
  float average = (redBlend + greenBlend) / 2.0;
  float blendAlpha = max(average, max(redBlend, greenBlend));

  fragColor = vec4(asColorSpace, 0.0, blendAlpha);
}
