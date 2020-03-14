#version 300 es

precision lowp float;

in vec2 v_texcoord;

in vec2[1] v_lights;
in vec2 v_relativeScreenCoords;

uniform sampler2D u_texture_game_albedo;
uniform sampler2D u_texture_game_normal;
uniform sampler2D u_texture_game_specular;

out vec4 fragColor;

vec4 calculateLight(vec2 light, float attenuation, vec3 lightColor, vec4 specularTexture, vec4 specularColor, vec4 normalTexture) {
  vec2 position = v_relativeScreenCoords;
  float lightAmount = clamp(1.0 - (distance(position, light) / attenuation), 0.0, 1.0);
  float specularAmountFromTexture = (specularTexture.r + specularTexture.g + specularTexture.b) / 3.0;

  vec3 normalFlipedY = vec3(normalTexture.r, 1.0 - normalTexture.g, normalTexture.b);
  vec3 normalTangent = (2.0f * normalFlipedY) - 1.0f;
  vec3 halfVec = vec3(0.0, 0.0, 1.0);

  vec3 lightDirNorm = normalize(vec3(light, 1.0) - vec3(position, 1.0));
  float specularAmount = max(dot(normalTangent, lightDirNorm), 0.0) * specularAmountFromTexture * (1.5 * lightAmount);

  vec3 reflection = normalize(vec3(2.0 * specularAmount) * (normalTangent - lightDirNorm));
  float specular = min(pow(clamp(dot(reflection, halfVec), 0.0, 1.0), 10.0), specularAmount);

  return mix(vec4(lightColor, lightAmount), specularColor, specularAmount);
}

void main(void) {

  // Lights
  vec4 specularTexture = texture(u_texture_game_specular, v_texcoord);
  vec4 normalTexture = texture(u_texture_game_normal, v_texcoord);

  vec4 lightColor = calculateLight(v_lights[0], 100.0, vec3(1.0, 0.0, 1.0), specularTexture, vec4(1.0), normalTexture);
  //

  fragColor = lightColor;
}
