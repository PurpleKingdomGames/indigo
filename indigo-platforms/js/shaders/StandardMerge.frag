#version 300 es

precision mediump float;

in vec2 v_texcoord;

in vec4 v_gameLayerOverlay;
in vec4 v_lightingLayerOverlay;
in vec4 v_uiLayerOverlay;

in vec4 v_gameLayerTint;
in vec4 v_lightingLayerTint;
in vec4 v_uiLayerTint;

in float v_gameLayerSaturation;
in float v_lightingLayerSaturation;
in float v_uiLayerSaturation;

uniform sampler2D u_texture_game;
uniform sampler2D u_texture_lighting;
uniform sampler2D u_texture_ui;

out vec4 fragColor;

vec4 grayscale(vec4 colour) {
  float average = (colour.r + colour.g + colour.b) / float(3);

  return vec4(average, average, average, colour.a);
}

vec4 applyEffects(vec4 diffuse, float saturation, vec4 tint, vec4 overlay) {

  vec4 withSaturation = mix(grayscale(diffuse), diffuse, max(0.0, saturation));

  vec4 tintedVersion = vec4(withSaturation.rgb * tint.rgb, withSaturation.a);

  vec4 withTint = mix(withSaturation, tintedVersion, max(0.0, tint.a));

  vec4 overlayVersion = vec4(overlay.rgb, withTint.a);

  vec4 withOverlay = mix(withTint, overlayVersion, max(0.0, overlay.a));

  return withOverlay;
}

void main(void) {

  vec4 textureColorGame = applyEffects(texture(u_texture_game, v_texcoord), v_gameLayerSaturation, v_gameLayerTint, v_gameLayerOverlay);
  vec4 textureColorLighting = applyEffects(texture(u_texture_lighting, v_texcoord), v_lightingLayerSaturation, v_lightingLayerTint, v_lightingLayerOverlay);
  vec4 textureColorUi = applyEffects(texture(u_texture_ui, v_texcoord), v_uiLayerSaturation, v_uiLayerTint, v_uiLayerOverlay);

  vec4 gameAndLighting = textureColorGame * textureColorLighting;

  fragColor = mix(gameAndLighting, textureColorUi, textureColorUi.a);
}
