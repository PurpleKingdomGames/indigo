#version 300 es

precision lowp float;

layout (location = 0) in vec4 a_verticesAndCoords; // a_vertices, a_texcoord
layout (location = 1) in vec4 a_matRotateScale; // mat(0,1,4,5)
layout (location = 2) in vec4 a_matTranslateAlpha; // mat(12,13,14,alpha)
layout (location = 3) in vec2 a_size; // 
layout (location = 4) in vec4 a_frameTransform; // a_frameTranslation, a_frameScale
layout (location = 5) in vec4 a_emissiveNormalOffsets; // a_emissive (vec2), a_normal (vec2)
layout (location = 6) in vec4 a_specularOffsetIsLitMatType; // a_specular (vec2), a_isLit (float), a_materialType (float)
layout (location = 7) in vec4 a_textureAmounts; // albedoAmount (float), emissiveAmount (float), normalAmount (float), specularAmount (float)

uniform mat4 u_projection;

// public
uniform float TIME; // Running time
uniform float TIME_DELTA; // Time delta between frames
uniform float WINDOW_SIZE; // Size of the viewport

// public
out vec2 TEXCOORDS; // Scaled to position on texture atlas
out vec2 UV; // Unscaled texture coordinates
out vec2 SIZE; // Width / height of the objects
out vec2 POSITION; // Position on the screen.
out float ALPHA; // Alpha of entity
//
out vec4 v_texcoordEmissiveNormal;
out vec4 v_textureAmounts;
out vec4 v_texcoordSpecularIsLitMatType;

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

vec4 VERTEX;

//#vertex_start
void vertex(){}
//#vertex_end

void main(void) {

  vec4 vertices = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
  vec2 texcoords = scaleTexCoordsWithOffset(a_verticesAndCoords.zw, a_frameTransform.xy);

  //
  vec2 texcoordsEmissive = a_emissiveNormalOffsets.xy;
  vec2 texcoordsNormal = a_emissiveNormalOffsets.zw;
  vec2 texcoordsSpecular = a_specularOffsetIsLitMatType.xy;
  float isLit = a_specularOffsetIsLitMatType.z;
  float materialType = a_specularOffsetIsLitMatType.w;

  mat4 transform =
    mat4(a_matRotateScale.x,    a_matRotateScale.y,    0,                     0,
         a_matRotateScale.z,    a_matRotateScale.w,    0,                     0,
         0,                     0,                     1,                     0,
         a_matTranslateAlpha.x, a_matTranslateAlpha.y, a_matTranslateAlpha.z, 1
        );

  VERTEX = u_projection * transform * vertices;
  
  vertex();

  gl_Position = VERTEX;

  TEXCOORDS = texcoords;
  UV = a_verticesAndCoords.zw;
  SIZE = a_size;
  POSITION = a_matTranslateAlpha.xy;
  ALPHA = a_matTranslateAlpha.w;

  //
  v_texcoordEmissiveNormal = vec4(scaleTexCoordsWithOffset(texcoords, texcoordsEmissive), scaleTexCoordsWithOffset(texcoords, texcoordsNormal));
  v_textureAmounts = a_textureAmounts;
  v_texcoordSpecularIsLitMatType = vec4(scaleTexCoordsWithOffset(texcoords, texcoordsSpecular), vec2(isLit, materialType));
}
