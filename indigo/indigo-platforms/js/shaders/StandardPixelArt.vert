#version 300 es

layout (location = 0) in vec4 a_verticesAndCoords; // a_vertices, a_texcoord
layout (location = 1) in vec4 a_transform; // a_translation, a_scale
layout (location = 2) in vec4 a_frameTransform; // a_frameTranslation, a_frameScale
layout (location = 3) in vec4 a_dimensions; // a_ref, a_size
layout (location = 4) in vec4 a_tint;
layout (location = 5) in vec4 a_gradiantPositions; // a_gradiantOverlayFrom, a_gradiantOverlayTo
layout (location = 6) in vec4 a_gradiantOverlayFromColor;
layout (location = 7) in vec4 a_gradiantOverlayToColor;
layout (location = 8) in vec4 a_borderColor;
layout (location = 9) in vec4 a_glowColor;
layout (location = 10) in vec4 a_amounts; // a_outerBorderAmount, a_innerBorderAmount, a_outerGlowAmount, a_innerGlowAmount
layout (location = 11) in vec4 a_rotationAlphaFlipHFlipV; // a_rotation, a_alpha, a_fliph, a_flipv

uniform mat4 u_projection;

out vec2 v_texcoord;
out vec2 v_size;

out vec4 v_tint;
out vec2 v_gradiantFrom;
out vec2 v_gradiantTo;
out vec4 v_gradiantOverlayFromColor;
out vec4 v_gradiantOverlayToColor;
out vec4 v_borderColor;
out vec4 v_glowColor;
out float v_outerBorderAmount;
out float v_innerBorderAmount;
out float v_outerGlowAmount;
out float v_innerGlowAmount;
out float v_alpha;

out vec2 v_textureOffsets3x3[9];
out vec2 v_textureOffsets5x5[25];
out vec2 v_textureOffsets7x7[49];
out vec2 v_textureOffsets9x9[81];
out vec2 v_relativeScreenCoords;

mat4 rotate2d(float angle){
    return mat4(cos(angle), -sin(angle), 0, 0,
                sin(angle), cos(angle), 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
                );
}

mat4 translate2d(vec2 t){
    return mat4(1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                t.x, t.y, 0, 1
                );
}

mat4 scale2d(vec2 s){
    return mat4(s.x, 0, 0, 0,
                0, s.y, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
                );
}

vec2 scaleTextCoords(vec2 texcoord){
  mat4 transform = translate2d(a_frameTransform.xy) * scale2d(a_frameTransform.zw);
  return (transform * vec4(texcoord, 1.0, 1.0)).xy;
}

vec2 sizeOfAPixel() {
  return (scale2d(a_frameTransform.zw) * vec4(1.0)).xy;
}

vec2[9] generate3x3() {
  vec2[9] res;
  float size = 3.0;
  int limit = 9;
  int count = 0;
  float offset = floor(size / 2.0);

  for(int i = 0; i < limit; i++) {
    float x = mod(float(i), size) - offset;
    float y = floor(float(i) / size) - offset;

    res[i] = vec2(x, y);
  }

  return res;
}

vec2[9] generateTexCoords3x3(vec2 texcoords, vec2 onePixel, vec2[9] offsets) {
  vec2[9] res;
  int limit = 9;

  for(int i = 0; i < limit; i++) {
    res[i] = scaleTextCoords(texcoords + (onePixel * offsets[i]));
  }

  return res;
}

// vec2[25] generate5x5() {
//   vec2[25] res;
//   float size = 5.0;
//   int limit = 25;
//   int count = 0;
//   float offset = floor(size / 2.0);

//   for(int i = 0; i < limit; i++) {
//     float x = mod(float(i), size) - offset;
//     float y = floor(float(i) / size) - offset;

//     res[i] = vec2(x, y);
//   }

//   return res;
// }

// vec2[25] generateTexCoords5x5(vec2 texcoords, vec2 onePixel, vec2[25] offsets) {
//   vec2[25] res;
//   int limit = 25;

//   for(int i = 0; i < limit; i++) {
//     res[i] = scaleTextCoords(texcoords + (onePixel * offsets[i]));
//   }

//   return res;
// }

void main(void) {

  vec4 vertices = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
  vec2 texcoords = a_verticesAndCoords.zw;
  vec2 ref = a_dimensions.xy;
  vec2 size = a_dimensions.zw;
  vec2 translation = a_transform.xy;
  vec2 scale = a_transform.zw;
  float rotation = a_rotationAlphaFlipHFlipV.x;
  float alpha = a_rotationAlphaFlipHFlipV.y;
  vec2 flip = a_rotationAlphaFlipHFlipV.zw;

  vec2 moveToReferencePoint = -(ref / size) + 0.5;

  mat4 transform = 
    translate2d(translation) * 
    rotate2d(rotation) * 
    scale2d(size * scale) * 
    translate2d(moveToReferencePoint) * 
    scale2d(flip);

  gl_Position = u_projection * transform * vertices;

  v_texcoord = scaleTextCoords(texcoords);
  v_size = size;

  v_tint = a_tint;
  v_gradiantFrom = a_gradiantPositions.xy;
  v_gradiantTo = a_gradiantPositions.zw;
  v_gradiantOverlayFromColor = a_gradiantOverlayFromColor;
  v_gradiantOverlayToColor = a_gradiantOverlayToColor;
  v_borderColor = a_borderColor;
  v_glowColor = a_glowColor;
  v_outerBorderAmount = a_amounts.x;
  v_innerBorderAmount = a_amounts.y;
  v_outerGlowAmount = a_amounts.z;
  v_innerGlowAmount = a_amounts.w;
  v_alpha = alpha;

  v_relativeScreenCoords = texcoords * size;

  vec2 onePixel = sizeOfAPixel();

  v_textureOffsets3x3 = generateTexCoords3x3(texcoords, onePixel, generate3x3());
  // v_textureOffsets5x5 = generateTexCoords5x5(texcoords, onePixel, generate5x5());

}
