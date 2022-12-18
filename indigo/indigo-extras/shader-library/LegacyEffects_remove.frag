#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;

vec4 CHANNEL_0;
vec4 COLOR;
vec2 UV;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform IndigoLegacyEffectsData {
  highp vec3 ALPHA_SATURATION_OVERLAYTYPE;
  vec4 TINT;
  vec4 GRADIENT_FROM_TO;
  vec4 GRADIENT_FROM_COLOR;
  vec4 GRADIENT_TO_COLOR;
  vec4 BORDER_COLOR;
  vec4 GLOW_COLOR;
  vec4 EFFECT_AMOUNTS;
};

//---- inherited from ImageEffects

vec4 applyBasicEffects(vec4 textureColor) {
  float alpha = ALPHA_SATURATION_OVERLAYTYPE.x;
  vec4 withAlpha = vec4(textureColor.rgb * alpha, textureColor.a * alpha);
  vec4 tintedVersion = vec4(withAlpha.rgb * TINT.rgb, withAlpha.a);

  return tintedVersion;
}

vec4 calculateColorOverlay(vec4 color) {
  return mix(color, vec4(GRADIENT_FROM_COLOR.rgb * color.a, color.a), GRADIENT_FROM_COLOR.a);
}

vec4 calculateLinearGradientOverlay(vec4 color) {
  vec2 pointA = GRADIENT_FROM_TO.xy;
  vec2 pointB = GRADIENT_FROM_TO.zw;
  vec2 pointP = UV * SIZE;

  // `h` is the distance along the gradient 0 at A, 1 at B
  float h = min(1.0, max(0.0, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)));

  vec4 gradient = mix(GRADIENT_FROM_COLOR, GRADIENT_TO_COLOR, h);

  return mix(color, vec4(gradient.rgb * color.a, color.a), gradient.a);
}

vec4 calculateRadialGradientOverlay(vec4 color) {
  vec2 pointA = GRADIENT_FROM_TO.xy;
  vec2 pointB = GRADIENT_FROM_TO.zw;
  vec2 pointP = UV * SIZE;

  float radius = length(pointB - pointA);
  float distanceToP = length(pointP - pointA);

  float sdf = clamp(-((distanceToP - radius) / radius), 0.0, 1.0);

  vec4 gradient = mix(GRADIENT_TO_COLOR, GRADIENT_FROM_COLOR, sdf);

  return mix(color, vec4(gradient.rgb * color.a, color.a), gradient.a);
}

vec4 calculateSaturation(vec4 color) {
  float saturation = ALPHA_SATURATION_OVERLAYTYPE.y;
  float average = (color.r + color.g + color.b) / float(3.0);
  vec4 grayscale = vec4(average, average, average, color.a);

  return mix(grayscale, color, max(0.0, min(1.0, saturation)));
}

//---- /inherited from ImageEffects
//---- legacy effects

in vec2 v_offsetTL;
in vec2 v_offsetTC;
in vec2 v_offsetTR;
in vec2 v_offsetML;
in vec2 v_offsetMC;
in vec2 v_offsetMR;
in vec2 v_offsetBL;
in vec2 v_offsetBC;
in vec2 v_offsetBR;

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

vec4 calculateOuterBorder(float baseAlpha, float[9] alphas, float amount) {
  vec4 outColor = vec4(0.0);
  float checkedAmount = clamp(amount, 0.0, 2.0);
  float[9] kernel;

  if(baseAlpha > 0.001) {
    return outColor;
  }
  
  if(abs(checkedAmount) >= 0.0 && abs(checkedAmount) < 0.01) {
    return outColor;
  }
  if(abs(checkedAmount) >= 0.99 && abs(checkedAmount) < 1.01) {
    kernel = border1px;
  }
  if(abs(checkedAmount) >= 1.99 && abs(checkedAmount) < 2.01) {
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
    outColor = BORDER_COLOR;
  }

  return outColor;
}

vec4 calculateInnerBorder(float baseAlpha, float[9] alphas, float amount) {
  vec4 outColor = vec4(0.0);
  float checkedAmount = clamp(amount, 0.0, 2.0);
  float[9] kernel;

  if(baseAlpha < 0.001) {
    return outColor;
  }

  if(abs(checkedAmount) >= 0.0 && abs(checkedAmount) < 0.01) {
    return outColor;
  }
  if(abs(checkedAmount) >= 0.99 && abs(checkedAmount) < 1.01) {
    kernel = border1px;
  }
  if(abs(checkedAmount) >= 1.99 && abs(checkedAmount) < 2.01) {
    kernel = border2px;
  }

  float alphaSum =
    floor(alphas[0]) * kernel[0] +
    floor(alphas[1]) * kernel[1] +
    floor(alphas[2]) * kernel[2] +
    floor(alphas[3]) * kernel[3] +
    floor(alphas[4]) * kernel[4] +
    floor(alphas[5]) * kernel[5] +
    floor(alphas[6]) * kernel[6] +
    floor(alphas[7]) * kernel[7] +
    floor(alphas[8]) * kernel[8];

  if(alphaSum > 0.0) {
    outColor = BORDER_COLOR;
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

vec4 calculateOuterGlow(float baseAlpha, float[9] alphas, float amount) {
  vec4 outColor = vec4(0.0);

  if(baseAlpha > 0.01) {
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
    outColor = vec4(GLOW_COLOR.rgb, GLOW_COLOR.a * glowAmount);
  }

  return outColor;
}

vec4 calculateInnerGlow(float baseAlpha, float[9] alphas, float amount) {
  vec4 outColor = vec4(0.0);

  if(baseAlpha < 0.01) {
    return outColor;
  }

  float alphaSum =
    floor(alphas[0]) * glowKernel[0] +
    floor(alphas[1]) * glowKernel[1] +
    floor(alphas[2]) * glowKernel[2] +
    floor(alphas[3]) * glowKernel[3] +
    floor(alphas[4]) * glowKernel[4] +
    floor(alphas[5]) * glowKernel[5] +
    floor(alphas[6]) * glowKernel[6] +
    floor(alphas[7]) * glowKernel[7] +
    floor(alphas[8]) * glowKernel[8];

  if(alphaSum > 0.0) {
    float checkedAmount = max(0.0, amount);
    float glowAmount = (alphaSum / glowKernelWeight) * checkedAmount;
    outColor = vec4(GLOW_COLOR.rgb, GLOW_COLOR.a * glowAmount);
  }

  return outColor;
}

//---- /legacy effects

void fragment(){

  vec4 baseColor = applyBasicEffects(CHANNEL_0);

  // 0 = color; 1 = linear gradient; 2 = radial gradient
  int overlayType = int(round(ALPHA_SATURATION_OVERLAYTYPE.z));
  vec4 overlay;

  switch(overlayType) {
    case 0:
      overlay = calculateColorOverlay(baseColor);
      break;

    case 1:
      overlay = calculateLinearGradientOverlay(baseColor);
      break;

    case 2:
      overlay = calculateRadialGradientOverlay(baseColor);
      break;

    default:
      overlay = calculateColorOverlay(baseColor);
      break;
  }

  vec4 saturation = calculateSaturation(overlay);

  COLOR = saturation;

  // Effects (everything above is a copy+paste from ImageEffects.frag)

  float[9] sampledRegionAlphas = float[9](
    texture(SRC_CHANNEL, v_offsetTL).a,
    texture(SRC_CHANNEL, v_offsetTC).a,
    texture(SRC_CHANNEL, v_offsetTR).a,
    texture(SRC_CHANNEL, v_offsetML).a,
    texture(SRC_CHANNEL, v_offsetMC).a,
    texture(SRC_CHANNEL, v_offsetMR).a,
    texture(SRC_CHANNEL, v_offsetBL).a,
    texture(SRC_CHANNEL, v_offsetBC).a,
    texture(SRC_CHANNEL, v_offsetBR).a
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

  float outerBorderAmount = EFFECT_AMOUNTS.x;
  float innerBorderAmount = EFFECT_AMOUNTS.y;
  float outerGlowAmount = EFFECT_AMOUNTS.z;
  float innerGlowAmount = EFFECT_AMOUNTS.w;

  vec4 innerGlow = calculateInnerGlow(COLOR.a, sampledRegionAlphasInverse, innerGlowAmount);
  vec4 outerGlow = calculateOuterGlow(COLOR.a, sampledRegionAlphas, outerGlowAmount);
  vec4 innerBorder = calculateInnerBorder(COLOR.a, sampledRegionAlphasInverse, innerBorderAmount);
  vec4 outerBorder = calculateOuterBorder(COLOR.a, sampledRegionAlphas, outerBorderAmount);

  vec4 withInnerGlow = vec4(mix(COLOR.rgb, innerGlow.rgb, innerGlow.a), COLOR.a);
  vec4 withOuterGlow = mix(withInnerGlow, outerGlow, outerGlow.a);
  vec4 withInnerBorder = mix(withOuterGlow, innerBorder, innerBorder.a);
  vec4 withOuterBorder = mix(withInnerBorder, outerBorder, outerBorder.a);

  COLOR = withOuterBorder;
}
//</indigo-fragment>
