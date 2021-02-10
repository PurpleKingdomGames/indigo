#version 300 es

precision mediump float;

vec4 CHANNEL_0;
vec4 COLOR;
vec2 UV;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform CustomData {
  float ALPHA;
  vec4 TINT;
  vec4 GRADIANT_FROM_TO;
  vec4 GRADIANT_FROM_COLOR;
  vec4 GRADIANT_TO_COLOR;
};

vec4 applyBasicEffects(vec4 textureColor) {
  vec4 withAlpha = vec4(textureColor.rgb, textureColor.a * ALPHA);

  vec4 tintedVersion = vec4(withAlpha.rgb * TINT.rgb, withAlpha.a);

  return mix(withAlpha, tintedVersion, max(0.0, TINT.a));
}

vec4 calculateGradiantOverlay() {
  vec2 screenRelativeCoords = UV * SIZE;

  vec2 pointA = GRADIANT_FROM_TO.xy;
  vec2 pointB = GRADIANT_FROM_TO.zw;
  vec2 pointP = screenRelativeCoords;

  // `h` is the distance along the gradiant 0 at A, 1 at B
  float h = min(1.0, max(0.0, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)));

  return mix(GRADIANT_FROM_COLOR, GRADIANT_TO_COLOR, h);
}

void fragment(){
  vec4 baseColor = applyBasicEffects(CHANNEL_0);
  vec4 overlay = calculateGradiantOverlay();
  vec4 withOverlay = vec4(mix(baseColor.rgb, overlay.rgb, overlay.a), baseColor.a);
  COLOR = withOverlay;
}
//</indigo-fragment>
