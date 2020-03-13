#version 300 es

precision lowp float;

in vec2 v_texcoord;

in vec4 v_gameOverlay;
in vec4 v_uiOverlay;

in vec4 v_gameLayerTint;
in vec4 v_lightingLayerTint;
in vec4 v_uiLayerTint;

in float v_gameLayerSaturation;
in float v_lightingLayerSaturation;
in float v_uiLayerSaturation;

in vec2[1] v_lights;
in vec2 v_relativeScreenCoords;
in vec2 v_screenCenterCoords;

uniform sampler2D u_texture_game_albedo;
uniform sampler2D u_texture_game_emissive;
uniform sampler2D u_texture_game_normal;
uniform sampler2D u_texture_game_specular;

out vec4 fragColor;

vec4 grayscale(vec4 colour) {
  float average = (colour.r + colour.g + colour.b) / float(3);

  return vec4(average, average, average, colour.a);
}

vec4 applyEffects(vec4 diffuse, float saturation, vec4 tint) {

  vec4 withSaturation = mix(grayscale(diffuse), diffuse, max(0.0, saturation));

  vec4 tintedVersion = vec4(withSaturation.rgb * tint.rgb, withSaturation.a);

  vec4 withTint = mix(withSaturation, tintedVersion, max(0.0, tint.a));

  return withTint;
}

vec4 applyOverlay(vec4 diffuse, vec4 overlay) {

  vec4 overlayVersion = vec4(overlay.rgb, diffuse.a);

  vec4 withOverlay = mix(diffuse, overlayVersion, max(0.0, min(1.0, overlay.a)));

  return withOverlay;
}

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

  vec4 textureColorGame = applyEffects(texture(u_texture_game_albedo, v_texcoord), v_gameLayerSaturation, v_gameLayerTint);

  // Basic lighting layer
  vec4 lighting = texture(u_texture_lighting, v_texcoord);
  vec4 textureColorLighting = applyEffects(lighting, v_lightingLayerSaturation, v_lightingLayerTint);

  // Emissive
  vec4 emissive = texture(u_texture_game_emissive, v_texcoord);
  vec4 textureColorEmissive = applyEffects(emissive, v_lightingLayerSaturation, v_lightingLayerTint);

  // Lights
  vec4 specularTexture = texture(u_texture_game_specular, v_texcoord);
  vec4 normalTexture = texture(u_texture_game_normal, v_texcoord);

  vec4 lightColor = calculateLight(v_lights[0], 100.0, vec3(1.0, 0.0, 1.0), specularTexture, vec4(1.0), normalTexture);
  //

  vec4 combinedLights = mix(textureColorLighting, lightColor, lightColor.a);

  vec4 gameAndLightingPlusEmissive = mix(textureColorGame * combinedLights, textureColorEmissive, textureColorEmissive.a);

  vec4 gameAndLightingPlusEmissiveAndOverlay = applyOverlay(gameAndLightingPlusEmissive, v_gameOverlay);

  vec4 textureColorUi = applyOverlay(applyEffects(texture(u_texture_ui, v_texcoord), v_uiLayerSaturation, v_uiLayerTint), v_uiOverlay);

  fragColor = mix(gameAndLightingPlusEmissiveAndOverlay, textureColorUi, textureColorUi.a);
}
