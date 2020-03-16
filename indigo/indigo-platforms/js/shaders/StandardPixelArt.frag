#version 300 es

precision lowp float;

uniform sampler2D u_texture;

in vec2 v_texcoord;
in vec2 v_texcoordEmissive;
in vec2 v_texcoordNormal;
in vec2 v_texcoordSpecular;
in float v_isLit;
in vec2 v_size;

in vec4 v_tint;
in vec2 v_gradiantFrom;
in vec2 v_gradiantTo;
in vec4 v_gradiantOverlayFromColor;
in vec4 v_gradiantOverlayToColor;
in vec4 v_borderColor;
in vec4 v_glowColor;
in float v_outerBorderAmount;
in float v_innerBorderAmount;
in float v_outerGlowAmount;
in float v_innerGlowAmount;
in float v_alpha;

in vec2 v_textureOffsets3x3[9];
in vec2 v_relativeScreenCoords;

layout(location = 0) out vec4 albedo;
layout(location = 1) out vec4 emissive;
layout(location = 2) out vec4 normal;
layout(location = 3) out vec4 specular;

vec4 applyBasicEffects(vec4 textureColor) {
  vec4 withAlpha = vec4(textureColor.rgb, textureColor.a * v_alpha);

  vec4 tintedVersion = vec4(withAlpha.rgb * v_tint.rgb, withAlpha.a);

  return mix(withAlpha, tintedVersion, max(0.0, v_tint.a));
}

vec4 calculateGradiantOverlay() {
  vec2 pointA = v_gradiantFrom;
  vec2 pointB = v_gradiantTo;
  vec2 pointP = v_relativeScreenCoords;

  // `h` is the distance along the gradiant 0 at A, 1 at B
  float h = min(1.0, max(0.0, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)));

  return mix(v_gradiantOverlayFromColor, v_gradiantOverlayToColor, h);
}

const float border1px[9] = float[9](
  0.0, 1.0, 0.0,
  1.0, 0.0, 1.0,
  0.0, 1.0, 0.0
);

const float border2px[9] = float[9](
  1.0, 1.0, 1.0,
  1.0, 0.0, 1.0,
  1.0, 1.0, 1.0
);

vec4 calculateBorder(float baseAlpha, float[9] alphas, float amount) {
  vec4 outColor = vec4(0.0);
  int checkedAmount = int(clamp(amount, 0.0, 2.0));

  if(checkedAmount == 0 || baseAlpha >= 0.0001) {
    return outColor;
  }

  float[9] kernel;

  if(checkedAmount == 1) {
    kernel = border1px;
  } else {
    kernel = border2px;
  }

  float alphaSum =
    alphas[0] * kernel[0] +
    alphas[1] * kernel[1] +
    alphas[2] * kernel[2] +
    alphas[3] * kernel[3] +
    alphas[4] * kernel[4] +
    alphas[5] * kernel[5] +
    alphas[6] * kernel[6] +
    alphas[7] * kernel[7] +
    alphas[8] * kernel[8];

  if(alphaSum > 0.0) {
    outColor = v_borderColor;
  }

  return outColor;
}

const float glowKernel[9] = float[9](
  1.0, 0.5, 1.0,
  0.5, 0.0, 0.5,
  1.0, 0.5, 1.0
);
// glowKernel values summed up.
const float glowKernelWeight = 6.0;

vec4 calculateGlow(float baseAlpha, float[9] alphas, float amount) {
  vec4 outColor = vec4(0.0);

  if(baseAlpha >= 0.0001) {
    return outColor;
  }

  float alphaSum =
    alphas[0] * glowKernel[0] +
    alphas[1] * glowKernel[1] +
    alphas[2] * glowKernel[2] +
    alphas[3] * glowKernel[3] +
    alphas[4] * glowKernel[4] +
    alphas[5] * glowKernel[5] +
    alphas[6] * glowKernel[6] +
    alphas[7] * glowKernel[7] +
    alphas[8] * glowKernel[8];

  if(alphaSum > 0.0) {
    float checkedAmount = max(0.0, amount);
    float glowAmount = (alphaSum / glowKernelWeight) * checkedAmount;
    outColor = vec4(v_glowColor.rgb, v_glowColor.a * glowAmount);
  }

  return outColor;
}

vec4 calculateNormal(vec4 normalColor, float alpha) {
  if (v_isLit > 0.0) {
    if(normalColor.a < 0.001) {
      return vec4(0.5, 0.5, 1.0, alpha);
    } else {
      return vec4(normalColor.rgb, alpha);
    }
  } else {
    return vec4(0.0, 0.0, 0.0, alpha);
  }
}

vec4 calculateSpecular(vec4 specularColor, float alpha) {
  if(specularColor.a < 0.001) {
    return vec4(1.0, 1.0, 1.0, alpha);
  } else {
    return vec4(specularColor.rgb, alpha);
  }
}

void main(void) {

  float[9] sampledRegionAlphas = float[9](
    applyBasicEffects(texture(u_texture, v_textureOffsets3x3[0])).a,
    applyBasicEffects(texture(u_texture, v_textureOffsets3x3[1])).a,
    applyBasicEffects(texture(u_texture, v_textureOffsets3x3[2])).a,
    applyBasicEffects(texture(u_texture, v_textureOffsets3x3[3])).a,
    applyBasicEffects(texture(u_texture, v_textureOffsets3x3[4])).a,
    applyBasicEffects(texture(u_texture, v_textureOffsets3x3[5])).a,
    applyBasicEffects(texture(u_texture, v_textureOffsets3x3[6])).a,
    applyBasicEffects(texture(u_texture, v_textureOffsets3x3[7])).a,
    applyBasicEffects(texture(u_texture, v_textureOffsets3x3[8])).a
  );

  float[9] sampledRegionAlphasInverse = float[9](
    (1.0 - sampledRegionAlphas[0]),
    (1.0 - sampledRegionAlphas[1]),
    (1.0 - sampledRegionAlphas[2]),
    (1.0 - sampledRegionAlphas[3]),
    (1.0 - sampledRegionAlphas[4]),
    (1.0 - sampledRegionAlphas[5]),
    (1.0 - sampledRegionAlphas[6]),
    (1.0 - sampledRegionAlphas[7]),
    (1.0 - sampledRegionAlphas[8])
  );

  vec4 baseColor = applyBasicEffects(texture(u_texture, v_texcoord));

  vec4 overlay = calculateGradiantOverlay();
  vec4 innerGlow = calculateGlow((1.0 - baseColor.a), sampledRegionAlphasInverse, v_innerGlowAmount);
  vec4 outerGlow = calculateGlow(baseColor.a, sampledRegionAlphas, v_outerGlowAmount);
  vec4 innerBorder = calculateBorder((1.0 - baseColor.a), sampledRegionAlphasInverse, v_innerBorderAmount);
  vec4 outerBorder = calculateBorder(baseColor.a, sampledRegionAlphas, v_outerBorderAmount);

  vec4 withOverlay = vec4(mix(baseColor.rgb, overlay.rgb, overlay.a), baseColor.a);
  vec4 withInnerGlow = vec4(mix(withOverlay.rgb, innerGlow.rgb, innerGlow.a), withOverlay.a);
  vec4 withOuterGlow = mix(withInnerGlow, outerGlow, outerGlow.a);
  vec4 withInnerBorder = mix(withOuterGlow, innerBorder, innerBorder.a);
  vec4 withOuterBorder = mix(withInnerBorder, outerBorder, outerBorder.a);

  vec4 outColor = withOuterBorder;

  albedo = outColor;

  if(v_texcoordEmissive == v_texcoord) {
    emissive = vec4(0.0);
  } else {
    emissive = texture(u_texture, v_texcoordEmissive);
  }

  if(v_texcoordNormal == v_texcoord) {
    normal = vec4(0.5, 0.5, 1.0, outColor.a);
  } else {
    normal = calculateNormal(texture(u_texture, v_texcoordNormal), outColor.a);
  }

  if(v_texcoordSpecular == v_texcoord) {
    specular = vec4(1.0, 1.0, 1.0, outColor.a);
  } else {
    specular = calculateSpecular(texture(u_texture, v_texcoordSpecular), outColor.a);
  }
}
