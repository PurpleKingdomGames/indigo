#version 300 es

precision mediump float;

vec4 CHANNEL_0;
vec4 COLOR;
vec2 UV;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform CustomData {
  highp vec3 ALPHA_SATURATION_OVERLAYTYPE;
  vec4 TINT;
  vec4 GRADIANT_FROM_TO;
  vec4 GRADIANT_FROM_COLOR;
  vec4 GRADIANT_TO_COLOR;
};

vec4 applyBasicEffects(vec4 textureColor) {
  float alpha = ALPHA_SATURATION_OVERLAYTYPE.x;
  vec4 withAlpha = vec4(textureColor.rgb * alpha, textureColor.a * alpha);
  vec4 tintedVersion = vec4(withAlpha.rgb * TINT.rgb, withAlpha.a);

  return tintedVersion;
}

vec4 calculateColorOverlay(vec4 color) {
  return mix(color, vec4(GRADIANT_FROM_COLOR.rgb * color.a, color.a), GRADIANT_FROM_COLOR.a);
}

vec4 calculateLinearGradiantOverlay(vec4 color) {
  vec2 pointA = GRADIANT_FROM_TO.xy;
  vec2 pointB = GRADIANT_FROM_TO.zw;
  vec2 pointP = UV * SIZE;

  // `h` is the distance along the gradiant 0 at A, 1 at B
  float h = min(1.0, max(0.0, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)));

  vec4 gradiant = mix(GRADIANT_FROM_COLOR, GRADIANT_TO_COLOR, h);

  return mix(color, vec4(gradiant.rgb * color.a, color.a), gradiant.a);
}

vec4 calculateRadialGradiantOverlay(vec4 color) {
  vec2 pointA = GRADIANT_FROM_TO.xy;
  vec2 pointB = GRADIANT_FROM_TO.zw;
  vec2 pointP = UV * SIZE;

  float radius = length(pointB - pointA);
  float distanceToP = length(pointP - pointA);

  float sdf = clamp(abs((distanceToP - radius) / radius), 0.0, 1.0);

  vec4 gradiant = mix(GRADIANT_TO_COLOR, GRADIANT_FROM_COLOR, sdf);

  return mix(color, vec4(gradiant.rgb * color.a, color.a), gradiant.a);
}

vec4 calculateSaturation(vec4 color) {
  float saturation = ALPHA_SATURATION_OVERLAYTYPE.y;
  float average = (color.r + color.g + color.b) / float(3.0);
  vec4 grayscale = vec4(average, average, average, color.a);

  return mix(grayscale, color, max(0.0, min(1.0, saturation)));
}

void fragment(){
  vec4 baseColor = applyBasicEffects(CHANNEL_0);

  // 0 = color; 1 = linear gradient; 2 = radial gradiant
  int overlayType = int(round(ALPHA_SATURATION_OVERLAYTYPE.z));
  vec4 overlay;

  switch(overlayType) {
    case 0:
      overlay = calculateColorOverlay(baseColor);
      break;

    case 1:
      overlay = calculateLinearGradiantOverlay(baseColor);
      break;

    case 2:
      overlay = calculateRadialGradiantOverlay(baseColor);
      break;

    default:
      overlay = calculateColorOverlay(baseColor);
      break;
  }

  vec4 saturation = calculateSaturation(overlay);

  COLOR = saturation;
}
//</indigo-fragment>
