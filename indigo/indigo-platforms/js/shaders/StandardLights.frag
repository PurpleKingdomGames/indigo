#version 300 es

precision lowp float;

in vec2 v_texcoord;

in vec2 v_relativeScreenCoords;

in float v_lightType;
in float v_lightAttenuation;
in vec2 v_lightPosition;
in vec3 v_lightColor;
in float v_lightRotation;
in float v_lightAngle;

uniform sampler2D u_texture_game_albedo;
uniform sampler2D u_texture_game_normal;
uniform sampler2D u_texture_game_specular;

out vec4 fragColor;

vec4 calculatePointLight(vec2 light, float attenuation, vec3 lightColor, vec4 specularTexture, vec3 specularColor, vec4 normalTexture, float masterAlpha) {
  vec2 position = v_relativeScreenCoords;
  float lightAmount = clamp(1.0 - (distance(position, light) / attenuation), 0.0, 1.0);
  float specularAmountFromTexture = (specularTexture.r + specularTexture.g + specularTexture.b) / 3.0;

  vec3 normalFlipedY = vec3(normalTexture.r, 1.0 - normalTexture.g, normalTexture.b);
  vec3 normalTangent = (2.0f * normalFlipedY) - 1.0f;
  vec3 halfVec = vec3(0.0, 0.0, 1.0);

  vec3 lightDirNorm = normalize(vec3(light, 1.0) - vec3(position, 1.0));
  float specularAmount = max(dot(normalTangent, lightDirNorm), 0.0) * lightAmount;

  vec3 reflection = normalize(vec3(2.0 * specularAmount) * (normalTangent - lightDirNorm));
  float specular = min(pow(clamp(dot(reflection, halfVec), 0.0, 1.0), 1.0), specularAmount) * specularAmountFromTexture;

  vec4 finalColor = mix(vec4(lightColor, lightAmount), vec4(specularColor, 1.0), specular);

  return vec4(finalColor.rgb, finalColor.a * masterAlpha);
}

vec4 calculateDirectionLight(vec3 lightColor, float rotation, float strength, vec4 specularTexture, vec3 specularColor, vec4 normalTexture, float masterAlpha) {
  vec2 position = v_relativeScreenCoords;
  float specularAmountFromTexture = clamp(((specularTexture.r + specularTexture.g + specularTexture.b) / 3.0), 0.0, 1.0);

  vec3 normalFlipedY = vec3(normalTexture.r, 1.0 - normalTexture.g, normalTexture.b);
  vec3 normalTangent = (2.0f * normalFlipedY) - 1.0f;
  vec3 halfVec = vec3(0.0, 0.0, 1.0);

  vec3 lightDirNorm = normalize(vec3(sin(rotation), cos(rotation), 0.1));
  float specularAmount = max(dot(normalTangent, lightDirNorm), 0.0);

  vec3 reflection = normalize(vec3(2.0 * specularAmount) * (normalTangent - lightDirNorm));
  float specular = min(pow(clamp(dot(reflection, halfVec), 0.0, 1.0), 1.0), specularAmount) * specularAmountFromTexture;

  vec4 finalColor = clamp(vec4(lightColor, strength) + vec4(specularColor, specular), 0.0, 1.0);

  return vec4(finalColor.rgb, finalColor.a * masterAlpha);
}

vec4 calculateSpotLight(vec2 light, float rotation, float attenuation, vec3 lightColor, vec4 specularTexture, vec3 specularColor, vec4 normalTexture, float masterAlpha, float lightAngle) {
  vec2 position = v_relativeScreenCoords;

  vec3 vectorToLight = normalize(vec3(light, 1.0) - vec3(position, 1.0));
  vec3 vectorFromLight = normalize(vec3(light, 1.0) - vec3(sin(rotation), cos(rotation), 1.0));

  float relativeAngle = dot(vectorToLight, vectorFromLight);
  
  float withinFrustrum = 0.0;

  if(relativeAngle < -0.99) {
    withinFrustrum = 1.0;
  }

  float lightAmount = clamp(1.0 - (distance(position, light) / attenuation), 0.0, 1.0);
  float specularAmountFromTexture = (specularTexture.r + specularTexture.g + specularTexture.b) / 3.0;

  vec3 normalFlipedY = vec3(normalTexture.r, 1.0 - normalTexture.g, normalTexture.b);
  vec3 normalTangent = (2.0f * normalFlipedY) - 1.0f;
  vec3 halfVec = vec3(0.0, 0.0, 1.0);

  vec3 lightDirNorm = normalize(vec3(light, 1.0) - vec3(position, 1.0));
  float specularAmount = max(dot(normalTangent, lightDirNorm), 0.0) * lightAmount;

  vec3 reflection = normalize(vec3(2.0 * specularAmount) * (normalTangent - lightDirNorm));
  float specular = min(pow(clamp(dot(reflection, halfVec), 0.0, 1.0), 1.0), specularAmount) * specularAmountFromTexture;

  vec4 finalColor = mix(vec4(lightColor, lightAmount), vec4(specularColor, 1.0), specular);

  // return vec4(finalColor.rgb, finalColor.a * masterAlpha);
  return vec4(withinFrustrum, withinFrustrum, withinFrustrum, 1.0);
}

void main(void) {

  vec4 albedoTexture = texture(u_texture_game_albedo, v_texcoord);
  vec4 specularTexture = texture(u_texture_game_specular, v_texcoord);
  vec4 normalTexture = texture(u_texture_game_normal, v_texcoord);

  vec4 lightColor = vec4(0.0);

  if(v_lightType == 1.0) {
    lightColor = calculatePointLight(v_lightPosition, v_lightAttenuation, v_lightColor, specularTexture, v_lightColor, normalTexture, albedoTexture.a);
  }

  if(v_lightType == 2.0) {
    lightColor = calculateDirectionLight(v_lightColor, v_lightRotation, v_lightAttenuation, specularTexture, v_lightColor, normalTexture, albedoTexture.a);
  }

  if(v_lightType == 3.0) {
    lightColor = calculateSpotLight(v_lightPosition, v_lightRotation, v_lightAttenuation, v_lightColor, specularTexture, v_lightColor, normalTexture, albedoTexture.a, v_lightAngle);
  }

  if(normalTexture.rgb == vec3(0.0)) {
    lightColor = vec4(0.0);
  }

  fragColor = lightColor;
}
