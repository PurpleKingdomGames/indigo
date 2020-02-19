#version 300 es

precision mediump float;

uniform sampler2D u_textureDiffuse;
uniform sampler2D u_textureEmission;
uniform sampler2D u_textureNormal;
uniform sampler2D u_textureSpecular;

in vec2 v_texcoord;
in vec4 v_tint;
in float v_alpha;
in vec2 v_size;
in vec2 v_textureOffsets3x3[9];
in vec2 v_textureOffsets5x5[25];
in vec2 v_relativeScreenCoords;

layout(location = 0) out vec4 fragColor0;
layout(location = 1) out vec4 fragColor1;

float gaussianBlur[9] = float[9](
  0.045, 0.122, 0.045,
  0.122, 0.332, 0.122,
  0.045, 0.122, 0.045
);

float gaussianBlur2[9] = float[9](
  1.0, 2.0, 1.0,
  2.0, 4.0, 2.0,
  1.0, 2.0, 1.0
);

float gaussianBlur3[9] = float[9](
  0.0, 1.0, 0.0,
  1.0, 1.0, 1.0,
  0.0, 1.0, 0.0
);

float border2px[9] = float[9](
  1.0, 1.0, 1.0,
  1.0, 0.0, 1.0,
  1.0, 1.0, 1.0
);

float glow2px[25] = float[25](
  1.0, 1.0, 1.0, 1.0, 1.0,
  1.0, 1.0, 1.0, 1.0, 1.0,
  1.0, 1.0, 0.0, 1.0, 1.0,
  1.0, 1.0, 1.0, 1.0, 1.0,
  1.0, 1.0, 1.0, 1.0, 1.0
);

vec4 applyBasicEffects(vec4 textureColor) {
  vec4 withAlpha = vec4(textureColor.rgb, textureColor.a * v_alpha);

  vec4 tintedVersion = vec4(withAlpha.rgb * v_tint.rgb, withAlpha.a);

  return mix(withAlpha, tintedVersion, max(0.0, v_tint.a));
}

float calculateWeight9(float kernel[9]) {
  float weight = 
    kernel[0] +
    kernel[1] +
    kernel[2] +
    kernel[3] +
    kernel[4] +
    kernel[5] +
    kernel[6] +
    kernel[7] +
    kernel[8];

  if (weight < 0.0) {
    weight = 1.0;
  }

  return weight;
}

float calculateWeight25(float kernel[25]) {
  float weight = 
    kernel[0] +
    kernel[1] +
    kernel[2] +
    kernel[3] +
    kernel[4] +
    kernel[5] +
    kernel[6] +
    kernel[7] +
    kernel[8] +
    kernel[9] +
    kernel[10] +
    kernel[11] +
    kernel[12] +
    kernel[13] +
    kernel[14] +
    kernel[15] +
    kernel[16] +
    kernel[17] +
    kernel[18] +
    kernel[19] +
    kernel[20] +
    kernel[21] +
    kernel[22] +
    kernel[23] +
    kernel[24];

  if (weight < 0.0) {
    weight = 1.0;
  }

  return weight;
}

void main(void) {

  // Guassian Blur
  // float blurType[9] = gaussianBlur;

  // vec4 colorSum =
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[0])) * blurType[0] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[1])) * blurType[1] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[2])) * blurType[2] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[3])) * blurType[3] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[4])) * blurType[4] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[5])) * blurType[5] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[6])) * blurType[6] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[7])) * blurType[7] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[8])) * blurType[8];

  // vec4 basicColor = colorSum / calculateWeight9(blurType);

  // Outer Border - 2 pixel
  // float borderKernel[9] = border2px;

  // float alphaSum =
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[0])).a * borderKernel[0] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[1])).a * borderKernel[1] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[2])).a * borderKernel[2] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[3])).a * borderKernel[3] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[4])).a * borderKernel[4] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[5])).a * borderKernel[5] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[6])).a * borderKernel[6] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[7])).a * borderKernel[7] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[8])).a * borderKernel[8];

  // vec4 basicColor = applyBasicEffects(texture(u_textureDiffuse, v_texcoord));

  // if(alphaSum > 0.0 && basicColor.a < 0.0001) {
  //   basicColor = vec4(0.0, 1.0, 1.0, 1.0);
  // }

  // Inner Border - 2 pixel
  // float borderKernel[9] = border2px;

  // float alphaSum =
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[0])).a) * borderKernel[0] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[1])).a) * borderKernel[1] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[2])).a) * borderKernel[2] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[3])).a) * borderKernel[3] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[4])).a) * borderKernel[4] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[5])).a) * borderKernel[5] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[6])).a) * borderKernel[6] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[7])).a) * borderKernel[7] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets3x3[8])).a) * borderKernel[8];

  // vec4 basicColor = applyBasicEffects(texture(u_textureDiffuse, v_texcoord));

  // if(alphaSum > 0.0 && (1.0 - basicColor.a) < 0.0001) {
  //   basicColor = vec4(0.0, 1.0, 1.0, 1.0);
  // }

  // Color Overlay
  // vec4 overlay = vec4(0.0, 1.0, 1.0, 1.0);
  // vec4 base = applyBasicEffects(texture(u_textureDiffuse, v_texcoord));

  // vec4 basicColor = vec4(mix(base.rgb, overlay.rgb, overlay.a), base.a);

  // Gradiant Overlay
  // vec4 colorA = vec4(1.0, 0.0, 1.0, 1.0);
  // vec4 colorB = vec4(0.0, 1.0, 1.0, 0.25);

  // vec2 pointA = vec2(0.0, 0.0);
  // vec2 pointB = v_size;
  // vec2 pointP = v_relativeScreenCoords;

  // // `h` is the distance along the gradiant 0 at A, 1 at B
  // float h = min(1.0, max(0.0, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)));

  // vec4 overlay = mix(colorA, colorB, h);

  // vec4 base = applyBasicEffects(texture(u_textureDiffuse, v_texcoord));
  // vec4 basicColor = vec4(mix(base.rgb, overlay.rgb, overlay.a), base.a);

  // Outer Glow - 2 pixel
  // float borderKernel[25] = glow2px;

  // float alphaSum =
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[0])).a * borderKernel[0] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[1])).a * borderKernel[1] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[2])).a * borderKernel[2] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[3])).a * borderKernel[3] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[4])).a * borderKernel[4] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[5])).a * borderKernel[5] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[6])).a * borderKernel[6] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[7])).a * borderKernel[7] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[8])).a * borderKernel[8] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[9])).a * borderKernel[9] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[10])).a * borderKernel[10] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[11])).a * borderKernel[11] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[12])).a * borderKernel[12] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[13])).a * borderKernel[13] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[14])).a * borderKernel[14] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[15])).a * borderKernel[15] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[16])).a * borderKernel[16] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[17])).a * borderKernel[17] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[18])).a * borderKernel[18] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[19])).a * borderKernel[19] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[20])).a * borderKernel[20] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[21])).a * borderKernel[21] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[22])).a * borderKernel[22] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[23])).a * borderKernel[23] +
  //   applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[24])).a * borderKernel[24];

  // vec4 basicColor = applyBasicEffects(texture(u_textureDiffuse, v_texcoord));

  // float glowAmount = alphaSum / calculateWeight25(borderKernel);

  // if(alphaSum > 0.0 && basicColor.a < 0.0001) {
  //   basicColor = vec4(0.0, 1.0, 1.0, glowAmount);
  // }

  // Inner Glow - 2 pixel
  // float borderKernel[25] = glow2px;

  // float alphaSum =
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[0])).a) * borderKernel[0] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[1])).a) * borderKernel[1] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[2])).a) * borderKernel[2] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[3])).a) * borderKernel[3] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[4])).a) * borderKernel[4] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[5])).a) * borderKernel[5] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[6])).a) * borderKernel[6] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[7])).a) * borderKernel[7] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[8])).a) * borderKernel[8] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[9])).a) * borderKernel[9] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[10])).a) * borderKernel[10] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[11])).a) * borderKernel[11] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[12])).a) * borderKernel[12] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[13])).a) * borderKernel[13] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[14])).a) * borderKernel[14] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[15])).a) * borderKernel[15] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[16])).a) * borderKernel[16] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[17])).a) * borderKernel[17] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[18])).a) * borderKernel[18] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[19])).a) * borderKernel[19] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[20])).a) * borderKernel[20] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[21])).a) * borderKernel[21] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[22])).a) * borderKernel[22] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[23])).a) * borderKernel[23] +
  //   (1.0 - applyBasicEffects(texture(u_textureDiffuse, v_textureOffsets5x5[24])).a) * borderKernel[24];

  // vec4 basicColor = applyBasicEffects(texture(u_textureDiffuse, v_texcoord));

  // float glowAmount = alphaSum / calculateWeight25(borderKernel);

  // if(alphaSum > 0.0 && (1.0 - basicColor.a) < 0.0001) {
  //   basicColor = mix(basicColor, vec4(0.0, 1.0, 1.0, 1.0), glowAmount);
  // }

  // Normal
  vec4 basicColor = applyBasicEffects(texture(u_textureDiffuse, v_texcoord));

  fragColor0 = basicColor;
  fragColor1 = vec4(0.0, 1.0, 0.0, basicColor.a);
}
