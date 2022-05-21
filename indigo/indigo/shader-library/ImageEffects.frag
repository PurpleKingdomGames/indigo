#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;

vec4 COLOR;
vec2 UV;
vec2 SIZE;
vec2 TEXTURE_SIZE;

vec4 CHANNEL_0;
vec2 CHANNEL_0_POSITION;
vec2 CHANNEL_0_SIZE;
vec4 CHANNEL_1;
vec2 CHANNEL_1_POSITION;
vec4 CHANNEL_2;
vec2 CHANNEL_2_POSITION;
vec4 CHANNEL_3;
vec2 CHANNEL_3_POSITION;

//<indigo-fragment>
layout (std140) uniform IndigoImageEffectsData {
  highp vec4 ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE;
  vec4 TINT;
  vec4 GRADIENT_FROM_TO;
  vec4 GRADIENT_FROM_COLOR;
  vec4 GRADIENT_TO_COLOR;
};

vec4 applyBasicEffects(vec4 textureColor) {
  float alpha = ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.x;
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
  float saturation = ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.y;
  float average = (color.r + color.g + color.b) / float(3.0);
  vec4 grayscale = vec4(average, average, average, color.a);

  return mix(grayscale, color, max(0.0, min(1.0, saturation)));
}

vec2 stretchedUVs(vec2 pos, vec2 size) {
  return pos + UV * size;
}

vec2 tiledUVs(vec2 pos, vec2 size) {
  return pos + (fract(UV * (SIZE / TEXTURE_SIZE)) * size);
}

void fragment(){

  // 0 = normal; 1 = stretch; 2 = tile
  int fillType = int(round(ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.w));

  switch(fillType) {
    case 1:
      CHANNEL_0 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_0_POSITION, CHANNEL_0_SIZE));
      CHANNEL_1 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_1_POSITION, CHANNEL_0_SIZE));
      CHANNEL_2 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_2_POSITION, CHANNEL_0_SIZE));
      CHANNEL_3 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_3_POSITION, CHANNEL_0_SIZE));
      break;

    case 2:
      CHANNEL_0 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_0_POSITION, CHANNEL_0_SIZE));
      CHANNEL_1 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_1_POSITION, CHANNEL_0_SIZE));
      CHANNEL_2 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_2_POSITION, CHANNEL_0_SIZE));
      CHANNEL_3 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_3_POSITION, CHANNEL_0_SIZE));
      break;

    default:
      break;
  }

  vec4 baseColor = applyBasicEffects(CHANNEL_0);

  // 0 = color; 1 = linear gradient; 2 = radial gradient
  int overlayType = int(round(ALPHA_SATURATION_OVERLAYTYPE_FILLTYPE.z));
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
}
//</indigo-fragment>
