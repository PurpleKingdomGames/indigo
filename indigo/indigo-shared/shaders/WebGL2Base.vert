#version 300 es

precision mediump float;

layout (location = 0) in vec4 a_verticesAndCoords;
layout (location = 1) in vec4 a_matRotateScale; // mat(0,1,4,5)
layout (location = 2) in vec4 a_matTranslateRotation; // mat(12,13,14), rotation angle
layout (location = 3) in vec4 a_sizeAndFrameScale;
layout (location = 4) in vec4 a_channelOffsets01;
layout (location = 5) in vec4 a_channelOffsets23;

// public
layout (std140) uniform IndigoProjectionData {
  mat4 u_projection;
};

layout (std140) uniform IndigoFrameData {
  float TIME; // Running time
  vec2 VIEWPORT_SIZE; // Size of the viewport in pixels
};

out vec4 v_channel_coords_01; // Scaled to position on texture atlas
out vec4 v_channel_coords_23; // Scaled to position on texture atlas
out vec4 v_uv_size; // Unscaled texture coordinates + Width / height of the objects
out vec3 v_screenCoordsRotation; // Where is this pixel on the screen? How much is it rotated by

// Constants
const float PI = 3.141592653589793;
const float PI_2 = PI * 0.5;
const float PI_4 = PI * 0.25;
const float TAU = 2.0 * PI;
const float TAU_2 = PI;
const float TAU_4 = PI_2;
const float TAU_8 = PI_4;

// Variables
vec4 VERTEX;
vec2 TEXTURE_COORDS;
vec2 UV;
vec2 SIZE;
vec2 CHANNEL_0_ATLAS_OFFSET;
vec2 CHANNEL_1_ATLAS_OFFSET;
vec2 CHANNEL_2_ATLAS_OFFSET;
vec2 CHANNEL_3_ATLAS_OFFSET;
vec2 CHANNEL_0_TEXTURE_COORDS;
vec2 CHANNEL_1_TEXTURE_COORDS;
vec2 CHANNEL_2_TEXTURE_COORDS;
vec2 CHANNEL_3_TEXTURE_COORDS;
float ROTATION;

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

vec2 scaleCoordsWithOffset(vec2 texcoord, vec2 offset){
  mat4 transform = translate2d(offset) * scale2d(a_sizeAndFrameScale.zw);
  return (transform * vec4(texcoord, 1.0, 1.0)).xy;
}

//#vertex_start
void vertex(){}
//#vertex_end

void main(void) {

  VERTEX = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
  TEXTURE_COORDS = a_verticesAndCoords.zw;
  UV = a_sizeAndFrameScale.zw;
  SIZE = a_sizeAndFrameScale.xy;
  v_uv_size = vec4(a_verticesAndCoords.zw, a_sizeAndFrameScale.xy);
  ROTATION = a_matTranslateRotation.w;
 
  CHANNEL_0_ATLAS_OFFSET = a_channelOffsets01.xy;
  CHANNEL_1_ATLAS_OFFSET = a_channelOffsets01.zw;
  CHANNEL_2_ATLAS_OFFSET = a_channelOffsets23.xy;
  CHANNEL_3_ATLAS_OFFSET = a_channelOffsets23.zw;
  CHANNEL_0_TEXTURE_COORDS = scaleCoordsWithOffset(TEXTURE_COORDS, CHANNEL_0_ATLAS_OFFSET);
  CHANNEL_1_TEXTURE_COORDS = scaleCoordsWithOffset(TEXTURE_COORDS, CHANNEL_1_ATLAS_OFFSET);
  CHANNEL_2_TEXTURE_COORDS = scaleCoordsWithOffset(TEXTURE_COORDS, CHANNEL_2_ATLAS_OFFSET);
  CHANNEL_3_TEXTURE_COORDS = scaleCoordsWithOffset(TEXTURE_COORDS, CHANNEL_3_ATLAS_OFFSET);

  vertex();

  mat4 transform =
    mat4(a_matRotateScale.x,       a_matRotateScale.y,       0,                        0,
         a_matRotateScale.z,       a_matRotateScale.w,       0,                        0,
         0,                        0,                        1,                        0,
         a_matTranslateRotation.x, a_matTranslateRotation.y, a_matTranslateRotation.z, 1
        );

  gl_Position = u_projection * transform * VERTEX;

  // vec2 screenCoords = (gl_Position.xy * 0.5 + 0.5) * VIEWPORT_SIZE;
  // vec2 screenCoords = (gl_Position.xy * 0.5 + 0.5) * (vec2(228, 128) * 2.0);
  // vec2 screenCoords = (gl_Position.xy * 0.5 + 0.5) * (vec2(1));
  vec2 screenCoords = gl_Position.xy * 0.5 + 0.5;

  v_screenCoordsRotation = vec3(screenCoords, ROTATION);

  v_channel_coords_01 = vec4(CHANNEL_0_TEXTURE_COORDS, CHANNEL_1_TEXTURE_COORDS);
  v_channel_coords_23 = vec4(CHANNEL_2_TEXTURE_COORDS, CHANNEL_3_TEXTURE_COORDS);
}
