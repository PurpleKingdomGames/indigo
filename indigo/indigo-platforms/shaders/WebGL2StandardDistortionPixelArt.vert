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

out vec2 v_texcoord;
out float v_alpha;

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

vec2 scaleTextCoords(){
  mat4 transform = translate2d(a_frameTransform.xy) * scale2d(a_frameTransform.zw);
  return (transform * vec4(a_verticesAndCoords.z, a_verticesAndCoords.w, 1, 1)).xy;
}

void main(void) {

  vec4 vertices = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
  vec2 texcoords = a_verticesAndCoords.zw;
  vec2 size = a_size;
  float alpha = a_matTranslateAlpha.w;

  mat4 transform =
    mat4(a_matRotateScale.x,    a_matRotateScale.y,    0,                     0,
         a_matRotateScale.z,    a_matRotateScale.w,    0,                     0,
         0,                     0,                     1,                     0,
         a_matTranslateAlpha.x, a_matTranslateAlpha.y, a_matTranslateAlpha.z, 1
        );

  gl_Position = u_projection * transform * vertices;

  // Pass the texcoord to the fragment shader.
  v_texcoord = scaleTextCoords();
  v_alpha = alpha;
}
