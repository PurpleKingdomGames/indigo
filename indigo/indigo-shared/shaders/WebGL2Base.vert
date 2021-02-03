#version 300 es

precision mediump float;

layout (location = 0) in vec4 a_verticesAndCoords;
layout (location = 1) in vec4 a_matRotateScale; // mat(0,1,4,5)
layout (location = 2) in vec3 a_matTranslate; // mat(12,13,14)
layout (location = 3) in vec4 a_sizeAndFrameScale;
layout (location = 4) in vec4 a_channelOffsets01;
layout (location = 5) in vec4 a_channelOffsets23;

// TODO: Is lit and matType to become flags.
// layout (location = 6) in vec4 a_specularOffsetIsLitMatType; // a_specular (vec2), a_isLit (float), a_materialType (float)

uniform mat4 u_projection;

// public
uniform float TIME; // Running time

out vec4 v_channel_coords_01; // Scaled to position on texture atlas
out vec4 v_channel_coords_23; // Scaled to position on texture atlas

// public
out vec4 v_uv_size; // Unscaled texture coordinates + Width / height of the objects
//
// out vec4 v_texcoordEmissiveNormal;
// out vec4 v_textureAmounts;
// out vec4 v_texcoordSpecularIsLitMatType;

// Constants
const float TAU = 2.0 * 3.141592653589793;
const float PI = 3.141592653589793;

// Variables
vec4 VERTEX;
vec2 TEXTURE_COORDS;

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
  mat4 transform = translate2d(offset) * scale2d(a_sizeAndFrameScale.zw);
  return (transform * vec4(texcoord, 1.0, 1.0)).xy;
}

//#vertex_start
void vertex(){}
//#vertex_end

void main(void) {

  VERTEX = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
  TEXTURE_COORDS = a_verticesAndCoords.zw;
  v_uv_size = vec4(a_verticesAndCoords.zw, a_sizeAndFrameScale.xy);

  vertex();

  mat4 transform =
    mat4(a_matRotateScale.x,    a_matRotateScale.y,    0,                     0,
         a_matRotateScale.z,    a_matRotateScale.w,    0,                     0,
         0,                     0,                     1,                     0,
         a_matTranslate.x,      a_matTranslate.y,      a_matTranslate.z,      1
        );

  gl_Position = u_projection * transform * VERTEX;

  v_channel_coords_01 = vec4(scaleTexCoordsWithOffset(TEXTURE_COORDS, a_channelOffsets01.xy), scaleTexCoordsWithOffset(TEXTURE_COORDS, a_channelOffsets01.zw));
  v_channel_coords_23 = vec4(scaleTexCoordsWithOffset(TEXTURE_COORDS, a_channelOffsets23.xy), scaleTexCoordsWithOffset(TEXTURE_COORDS, a_channelOffsets23.zw));

  //
  // float isLit = a_specularOffsetIsLitMatType.z;
  // float materialType = a_specularOffsetIsLitMatType.w;
  // v_texcoordSpecularIsLitMatType = vec4(scaleTexCoordsWithOffset(texcoords, texcoordsSpecular), vec2(isLit, materialType));
}
