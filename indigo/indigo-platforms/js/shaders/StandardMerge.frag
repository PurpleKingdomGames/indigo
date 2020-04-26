#version 300 es

precision lowp float;

in vec2 v_texcoord;
in vec2 v_size;

in vec4 v_gameOverlay;
in vec4 v_uiOverlay;

in vec4 v_gameLayerTint;
in vec4 v_lightingLayerTint;
in vec4 v_uiLayerTint;

in float v_gameLayerSaturation;
in float v_lightingLayerSaturation;
in float v_uiLayerSaturation;

uniform sampler2D u_texture_game_albedo;
uniform sampler2D u_texture_game_emissive;
uniform sampler2D u_texture_lights;
uniform sampler2D u_texture_lighting;
uniform sampler2D u_texture_distortion;
uniform sampler2D u_texture_ui;

out vec4 fragColor;

//----
// height to normal

// float grayscaleRGB(vec4 colour) {
//   return (colour.r + colour.g + colour.b) / 3.0;
// }

// float makeSample(vec2 at) {
//   return grayscaleRGB(texture(u_texture_lighting, at));
// }

// float rateOfChange(float sample1, float sample2, float sample3) {
//   return (sample1 - sample2) + (sample2 - sample3);
// }

vec2 calculationDisortionOffset(vec2 texcoord) {

  vec4 normalTexture = texture(u_texture_distortion, texcoord);

  if(normalTexture == vec4(0.0)) {
    normalTexture = vec4(0.5, 0.5, 0.0, 0.5);
  }

  // Normal
  vec3 normalFlipedY = vec3(normalTexture.r, 1.0 - normalTexture.g, normalTexture.b); // Flip Y
  vec2 normal = normalize((2.0 * normalFlipedY) - 1.0).rg; // Convert RGB 0 to 1, into -1 to 1

  float oneWidth = 1.0 / v_size.x;
  float oneHeight = 1.0 / v_size.y;

  // float x = rateOfChange(
  //   makeSample(vec2(texcoord.x - oneWidth, texcoord.y)),
  //   makeSample(texcoord),
  //   makeSample(vec2(texcoord.x + oneWidth, texcoord.y))
  // );

  // float y = rateOfChange(
  //   makeSample(vec2(texcoord.x, texcoord.y - oneHeight)),
  //   makeSample(texcoord),
  //   makeSample(vec2(texcoord.x, texcoord.y + oneHeight))
  // );

  float amount = 20.0;
  
  vec2 distorted = texcoord + vec2(oneWidth * (normal.x * amount), oneHeight * (normal.y * amount));

  return distorted;

  // return texture(u_texture_distortion, texcoord).rg;
}
//----

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

void main(void) {

  vec2 texcoord = calculationDisortionOffset(v_texcoord);

  vec4 textureColorGame = applyEffects(texture(u_texture_game_albedo, texcoord), v_gameLayerSaturation, v_gameLayerTint);

  // Basic lighting layer
  vec4 lighting = texture(u_texture_lighting, texcoord);
  vec4 textureColorLighting = applyEffects(lighting, v_lightingLayerSaturation, v_lightingLayerTint);

  // Emissive
  vec4 emissive = texture(u_texture_game_emissive, texcoord);
  vec4 textureColorEmissive = applyEffects(emissive, v_lightingLayerSaturation, v_lightingLayerTint);

  // Lights
  vec4 lights = texture(u_texture_lights, texcoord);
  vec4 textureColorLights = applyEffects(lights, v_lightingLayerSaturation, v_lightingLayerTint);

  vec4 combinedLights = mix(textureColorLighting, textureColorLights, textureColorLights.a);

  vec4 gameAndLightingPlusEmissive = mix(textureColorGame * combinedLights, textureColorEmissive, textureColorEmissive.a);

  vec4 gameAndLightingPlusEmissiveAndOverlay = applyOverlay(gameAndLightingPlusEmissive, v_gameOverlay);

  vec4 textureColorUi = applyOverlay(applyEffects(texture(u_texture_ui, texcoord), v_uiLayerSaturation, v_uiLayerTint), v_uiOverlay);

  fragColor = mix(gameAndLightingPlusEmissiveAndOverlay, textureColorUi, textureColorUi.a);
}
/*

        varying vec2 vcoord;
        varying vec2 vsize;

        float strength = 0.5;
        float minLevel = 0.0;
        float maxLevel = 255.0;
        float gamma = 1.5;

        float grayscaleRGB(vec4 colour) {
          return (colour.r + colour.g + colour.b) / 3.0;
        }

        float makeSample(vec2 at) {
          return grayscaleRGB(texture2D(texture, at));
        }

        float rateOfChange(float sample1, float sample2, float sample3) {
          return ((sample1 - sample2) + (sample2 - sample3)) / strength;
        }

        float levelRange(float color, float minInput, float maxInput){
            return min(max(color - minInput, 0.0) / (maxInput - minInput), 1.0);
        }

        float gammaCorrect(float value, float gamma){
          return pow(value, 1.0 / gamma);
        }

        float finalLevels(float color, float minInput, float gamma, float maxInput){
            return gammaCorrect(levelRange(color, minInput, maxInput), gamma);
        }

        vec4 heightToNormal() {

          float oneWidth = 1.0 / vsize.x;
          float oneHeight = 1.0 / vsize.y;

          float r = rateOfChange(
            makeSample(vec2(vcoord.x - oneWidth, vcoord.y)),
            makeSample(vcoord),
            makeSample(vec2(vcoord.x + oneWidth, vcoord.y))
          );

          float g = rateOfChange(
            makeSample(vec2(vcoord.x, vcoord.y - oneHeight)),
            makeSample(vcoord),
            makeSample(vec2(vcoord.x, vcoord.y + oneHeight))
          );

          float b = 1.0 - ((r + g) / 2.0);

          float rGamma = finalLevels(r + 0.5, minLevel/255.0, gamma, maxLevel/255.0);
          float gGamma = finalLevels(g + 0.5, minLevel/255.0, gamma, maxLevel/255.0);

          return vec4(rGamma, gGamma, b, 1.0);
        }

*/
