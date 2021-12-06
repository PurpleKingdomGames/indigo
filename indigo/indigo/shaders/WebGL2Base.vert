#version 300 es

precision mediump float;

layout (location = 0) in vec4 a_verticesAndCoords;
layout (location = 1) in vec4 a_translateScale;
layout (location = 2) in vec4 a_refFlip;
layout (location = 3) in vec4 a_sizeAndFrameScale;
layout (location = 4) in vec4 a_channelOffsets01;
layout (location = 5) in vec4 a_channelOffsets23;
layout (location = 6) in vec4 a_textureSizeAtlasSize;
layout (location = 7) in float a_rotation;
// public
layout (std140) uniform IndigoProjectionData {
  mat4 u_projection;
};

layout (std140) uniform IndigoFrameData {
  float TIME; // Running time
  vec2 VIEWPORT_SIZE; // Size of the viewport in pixels
};

// Used during cloning.
layout (std140) uniform IndigoCloneReferenceData {
  vec4 u_ref_refFlip;
  vec4 u_ref_sizeAndFrameScale;
  vec4 u_ref_channelOffsets01;
  vec4 u_ref_channelOffsets23;
  vec4 u_ref_textureSizeAtlasSize;
};

uniform mat4 u_baseTransform;
uniform int u_mode;

out vec4 v_channel_coords_01; // Scaled to position on texture atlas
out vec4 v_channel_coords_23; // Scaled to position on texture atlas
out vec4 v_uv_size; // Unscaled texture coordinates + Width / height of the objects
out vec3 v_screenCoordsRotation; // Where is this pixel on the screen? How much is it rotated by
out vec2 v_textureSize; // Actual size of the texture in pixels.
out vec4 v_atlasSizeAsUV; // Actual size of the atlas in pixels, and it's relative size in UV coords.
out vec4 v_channel_pos_01; // Position on the atlas of channels 0 and 1.
out vec4 v_channel_pos_23; // Position on the atlas of channels 2 and 3.
flat out int v_instanceId; // The current instance id

// Constants
const float PI = 3.141592653589793;
const float PI_2 = PI * 0.5;
const float PI_4 = PI * 0.25;
const float TAU = 2.0 * PI;
const float TAU_2 = PI;
const float TAU_4 = PI_2;
const float TAU_8 = PI_4;

// Variables
vec2 ATLAS_SIZE;
vec4 VERTEX;
vec2 TEXTURE_SIZE;
vec2 UV;
vec2 SIZE;
vec2 FRAME_SIZE;
vec2 CHANNEL_0_ATLAS_OFFSET;
vec2 CHANNEL_1_ATLAS_OFFSET;
vec2 CHANNEL_2_ATLAS_OFFSET;
vec2 CHANNEL_3_ATLAS_OFFSET;
vec2 CHANNEL_0_TEXTURE_COORDS;
vec2 CHANNEL_1_TEXTURE_COORDS;
vec2 CHANNEL_2_TEXTURE_COORDS;
vec2 CHANNEL_3_TEXTURE_COORDS;
vec2 CHANNEL_0_POSITION;
vec2 CHANNEL_1_POSITION;
vec2 CHANNEL_2_POSITION;
vec2 CHANNEL_3_POSITION;
vec2 CHANNEL_0_SIZE;
vec2 POSITION;
vec2 SCALE;
vec2 REF;
vec2 FLIP;
float ROTATION;
vec2 TEXTURE_COORDS; // Redundant, equal to UV
int INSTANCE_ID;

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

mat4 rotate2d(float angle){
    return mat4(cos(angle), -sin(angle), 0, 0,
                sin(angle), cos(angle), 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
                );
}

vec2 scaleCoordsWithOffset(vec2 texcoord, vec2 offset){
  mat4 transform = translate2d(offset) * scale2d(FRAME_SIZE);
  return (transform * vec4(texcoord, 1.0, 1.0)).xy;
}

//#vertex_start
void vertex(){}
//#vertex_end

void main(void) {

    INSTANCE_ID = gl_InstanceID;

    VERTEX = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
    UV = a_verticesAndCoords.zw;
    ROTATION = a_rotation;
    POSITION = a_translateScale.xy;
    SCALE = a_translateScale.zw;
    TEXTURE_COORDS = UV; // Deprecated

    // 0 = normal, 1 = clone batch, 2 = clone tiles
    switch(u_mode) {
      case 0:
        ATLAS_SIZE = a_textureSizeAtlasSize.zw;
        TEXTURE_SIZE = a_textureSizeAtlasSize.xy;
        SIZE = a_sizeAndFrameScale.xy;
        FRAME_SIZE = a_sizeAndFrameScale.zw;
        REF = a_refFlip.xy;
        FLIP = a_refFlip.zw;
        CHANNEL_0_ATLAS_OFFSET = a_channelOffsets01.xy;
        CHANNEL_1_ATLAS_OFFSET = a_channelOffsets01.zw;
        CHANNEL_2_ATLAS_OFFSET = a_channelOffsets23.xy;
        CHANNEL_3_ATLAS_OFFSET = a_channelOffsets23.zw;
        break;

      case 1:
        ATLAS_SIZE = u_ref_textureSizeAtlasSize.zw;
        TEXTURE_SIZE = u_ref_textureSizeAtlasSize.xy;
        SIZE = u_ref_sizeAndFrameScale.xy;
        FRAME_SIZE = u_ref_sizeAndFrameScale.zw;
        REF = u_ref_refFlip.xy;
        FLIP = u_ref_refFlip.zw;
        CHANNEL_0_ATLAS_OFFSET = u_ref_channelOffsets01.xy;
        CHANNEL_1_ATLAS_OFFSET = u_ref_channelOffsets01.zw;
        CHANNEL_2_ATLAS_OFFSET = u_ref_channelOffsets23.xy;
        CHANNEL_3_ATLAS_OFFSET = u_ref_channelOffsets23.zw;
        break;

      case 2:
        ATLAS_SIZE = u_ref_textureSizeAtlasSize.zw;
        TEXTURE_SIZE = u_ref_textureSizeAtlasSize.xy;
        SIZE = a_sizeAndFrameScale.xy;
        FRAME_SIZE = a_sizeAndFrameScale.zw;
        REF = u_ref_refFlip.xy;
        FLIP = u_ref_refFlip.zw;
        CHANNEL_0_ATLAS_OFFSET = a_channelOffsets01.xy;
        CHANNEL_1_ATLAS_OFFSET = a_channelOffsets01.zw;
        CHANNEL_2_ATLAS_OFFSET = a_channelOffsets23.xy;
        CHANNEL_3_ATLAS_OFFSET = a_channelOffsets23.zw;
        break;

      default:
        //
        break;
    }

  CHANNEL_0_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_0_ATLAS_OFFSET);
  CHANNEL_1_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_1_ATLAS_OFFSET);
  CHANNEL_2_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_2_ATLAS_OFFSET);
  CHANNEL_3_TEXTURE_COORDS = scaleCoordsWithOffset(UV, CHANNEL_3_ATLAS_OFFSET);
  CHANNEL_0_POSITION = scaleCoordsWithOffset(vec2(0.0), CHANNEL_0_ATLAS_OFFSET);
  CHANNEL_1_POSITION = scaleCoordsWithOffset(vec2(0.0), CHANNEL_1_ATLAS_OFFSET);
  CHANNEL_2_POSITION = scaleCoordsWithOffset(vec2(0.0), CHANNEL_2_ATLAS_OFFSET);
  CHANNEL_3_POSITION = scaleCoordsWithOffset(vec2(0.0), CHANNEL_3_ATLAS_OFFSET);
  CHANNEL_0_SIZE = TEXTURE_SIZE / ATLAS_SIZE;

  vertex();

  mat4 transform = 
    translate2d(POSITION) *
    rotate2d(-1.0 * ROTATION) *
    scale2d(SIZE * SCALE) *
    translate2d(-(REF / SIZE) + 0.5) *
    scale2d(vec2(1.0, -1.0) * FLIP);

  gl_Position = u_projection * u_baseTransform * transform * VERTEX;

  vec2 screenCoords = gl_Position.xy * 0.5 + 0.5;
  v_screenCoordsRotation = vec3(vec2(screenCoords.x, 1.0 - screenCoords.y) * VIEWPORT_SIZE, ROTATION);

  v_uv_size = vec4(UV, SIZE);
  v_channel_coords_01 = vec4(CHANNEL_0_TEXTURE_COORDS, CHANNEL_1_TEXTURE_COORDS);
  v_channel_coords_23 = vec4(CHANNEL_2_TEXTURE_COORDS, CHANNEL_3_TEXTURE_COORDS);
  v_textureSize = TEXTURE_SIZE, CHANNEL_0_POSITION;
  v_atlasSizeAsUV = vec4(ATLAS_SIZE, CHANNEL_0_SIZE);
  v_channel_pos_01 = vec4(CHANNEL_0_POSITION, CHANNEL_1_POSITION);
  v_channel_pos_23 = vec4(CHANNEL_2_POSITION, CHANNEL_3_POSITION);
  v_instanceId = INSTANCE_ID;
}
