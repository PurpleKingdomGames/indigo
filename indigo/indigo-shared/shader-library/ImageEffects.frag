#version 300 es

precision mediump float;

vec4 CHANNEL_0;
vec4 COLOR;
vec2 UV;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform CustomData {
  vec2 ALPHA_SATURATION;
  vec4 TINT;
  vec4 GRADIANT_FROM_TO;
  vec4 GRADIANT_FROM_COLOR;
  vec4 GRADIANT_TO_COLOR;
};

vec4 applyBasicEffects(vec4 textureColor) {
  float alpha = ALPHA_SATURATION.x;
  vec4 withAlpha = vec4(textureColor.rgb * alpha, textureColor.a * alpha);
  vec4 tintedVersion = vec4(withAlpha.rgb * TINT.rgb, withAlpha.a);

  return tintedVersion;
}

vec4 calculateGradiantOverlay(vec4 color) {
  vec2 screenRelativeCoords = UV * SIZE;

  vec2 pointA = GRADIANT_FROM_TO.xy;
  vec2 pointB = GRADIANT_FROM_TO.zw;
  vec2 pointP = screenRelativeCoords;

  // `h` is the distance along the gradiant 0 at A, 1 at B
  float h = min(1.0, max(0.0, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)));

  vec4 gradiant = mix(GRADIANT_FROM_COLOR, GRADIANT_TO_COLOR, h);

  return mix(color, vec4(gradiant.rgb * color.a, color.a), gradiant.a);
}

vec4 calculateSaturation(vec4 color) {
  float saturation = ALPHA_SATURATION.y;
  float average = (color.r + color.g + color.b) / float(3.0);
  vec4 grayscale = vec4(average, average, average, color.a);

  return mix(grayscale, color, max(0.0, min(1.0, saturation)));
}

void fragment(){
  vec4 baseColor = applyBasicEffects(CHANNEL_0);
  vec4 overlay = calculateGradiantOverlay(baseColor);
  vec4 saturation = calculateSaturation(overlay);
  COLOR = saturation;
}
//</indigo-fragment>
