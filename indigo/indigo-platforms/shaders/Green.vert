#version 300 es

precision lowp float;

layout (location = 0) in vec4 a_verticesAndCoords; // a_vertices, a_texcoord
layout (location = 1) in vec4 a_matRotateScale; // mat(0,1,4,5)
layout (location = 2) in vec4 a_matTranslateAlpha; // mat(12,13,14,alpha)
layout (location = 3) in vec2 a_size; // mat(12,13,14,alpha)
layout (location = 4) in vec4 a_frameTransform; // a_frameTranslation, a_frameScale
layout (location = 5) in vec4 a_tint;
layout (location = 6) in vec4 a_gradiantPositions; // a_gradiantOverlayFrom, a_gradiantOverlayTo
layout (location = 7) in vec4 a_gradiantOverlayFromColor;
layout (location = 8) in vec4 a_gradiantOverlayToColor;
layout (location = 9) in vec4 a_borderColor;
layout (location = 10) in vec4 a_glowColor;
layout (location = 11) in vec4 a_amounts; // a_outerBorderAmount, a_innerBorderAmount, a_outerGlowAmount, a_innerGlowAmount
layout (location = 12) in vec4 a_emissiveNormalOffsets; // a_emissive (vec2), a_normal (vec2)
layout (location = 13) in vec4 a_specularOffsetIsLit; // a_specular (vec2), a_isLit (float), ???
layout (location = 14) in vec4 a_textureAmounts; // albedoAmount (float), emissiveAmount (float), normalAmount (float), specularAmount (float)

uniform mat4 u_projection;

// The varying values are organised this way to help the packer
// squash them into 15 varying vectors
out vec4 v_texcoordEmissiveNormal;
out vec4 v_relativeScreenCoordsIsLitAlpha;
out vec4 v_tint;
out vec4 v_gradiantFromTo;
out vec4 v_gradiantOverlayFromColor;
out vec4 v_gradiantOverlayToColor;
out vec4 v_borderColor;
out vec4 v_glowColor;
out vec4 v_effectAmounts;
out vec4 v_textureAmounts;
out vec2 v_offsetTL;
out vec2 v_offsetTC;
out vec2 v_offsetTR;
out vec2 v_offsetML;
out vec2 v_offsetMC;
out vec2 v_offsetMR;
out vec2 v_offsetBL;
out vec2 v_offsetBC;
out vec2 v_offsetBR;
out vec2 v_texcoordSpecular;

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

vec2 scaleTexCoordsWithOffset(vec2 texcoord, vec2 offset){
  mat4 transform = translate2d(offset) * scale2d(a_frameTransform.zw);
  return (transform * vec4(texcoord, 1.0, 1.0)).xy;
}

vec2 scaleTexCoords(vec2 texcoord){
  return scaleTexCoordsWithOffset(texcoord, a_frameTransform.xy);
}

vec2 sizeOfAPixel() {
  return (scale2d(1.0 / a_size) * vec4(1.0)).xy;
}

const vec2[9] gridOffsets = vec2[9](
  vec2(-1.0, -1.0),
  vec2(0.0, -1.0),
  vec2(1.0, -1.0),

  vec2(-1.0, 0.0),
  vec2(0.0, 0.0),
  vec2(1.0, 0.0),

  vec2(-1.0, 1.0),
  vec2(0.0, 1.0),
  vec2(1.0, 1.0)
);

vec2[9] generateTexCoords3x3(vec2 texcoords, vec2 onePixel) {
  return vec2[9](
    scaleTexCoords(texcoords + (onePixel * gridOffsets[0])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[1])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[2])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[3])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[4])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[5])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[6])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[7])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[8]))
  );
}

void main(void) {

  vec4 vertices = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
  vec2 texcoords = a_verticesAndCoords.zw;
  float alpha = a_matTranslateAlpha.w;
  vec2 texcoordsEmissive = a_emissiveNormalOffsets.xy;
  vec2 texcoordsNormal = a_emissiveNormalOffsets.zw;
  vec2 texcoordsSpecular = a_specularOffsetIsLit.xy;
  float isLit = a_specularOffsetIsLit.z;

  vec2 offsets[9] = generateTexCoords3x3(texcoords, sizeOfAPixel());

  mat4 transform =
    mat4(a_matRotateScale.x,    a_matRotateScale.y,    0,                     0,
         a_matRotateScale.z,    a_matRotateScale.w,    0,                     0,
         0,                     0,                     1,                     0,
         a_matTranslateAlpha.x, a_matTranslateAlpha.y, a_matTranslateAlpha.z, 1
        );

  gl_Position = u_projection * transform * vertices;

  v_texcoordEmissiveNormal = vec4(scaleTexCoordsWithOffset(texcoords, texcoordsEmissive), scaleTexCoordsWithOffset(texcoords, texcoordsNormal));
  v_relativeScreenCoordsIsLitAlpha = vec4(texcoords * a_size, isLit, alpha);
  v_tint = a_tint;
  v_gradiantFromTo = a_gradiantPositions;
  v_gradiantOverlayFromColor = a_gradiantOverlayFromColor;
  v_gradiantOverlayToColor = a_gradiantOverlayToColor;
  v_borderColor = a_borderColor;
  v_glowColor = a_glowColor;
  v_effectAmounts = a_amounts;
  v_textureAmounts = a_textureAmounts;
  v_offsetTL = offsets[0];
  v_offsetTC = offsets[1];
  v_offsetTR = offsets[2];
  v_offsetML = offsets[3];
  v_offsetMC = offsets[4];
  v_offsetMR = offsets[5];
  v_offsetBL = offsets[6];
  v_offsetBC = offsets[7];
  v_offsetBR = offsets[8];
  v_texcoordSpecular = scaleTexCoordsWithOffset(texcoords, texcoordsSpecular);


}
