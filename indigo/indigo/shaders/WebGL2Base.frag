#version 300 es

precision mediump float;

layout(location = 0) out vec4 fragColor;

// ** Uniforms **
// Currently we only ever bind one texture at a time.
// The texture is however an atlas of textures, so in
// practice you can read many sub-textures at once.
// Could remove this limitation.
uniform sampler2D SRC_CHANNEL;

// public
layout (std140) uniform IndigoFrameData {
  float TIME; // Running time
  vec2 VIEWPORT_SIZE; // Size of the viewport in pixels
};


layout (std140) uniform IndigoDynamicLightingData {
  float numOfLights;
  highp vec4 lightFlags[8]; // vec4(active, type, far cut off, falloff type)
  vec4 lightColor[8];
  vec4 lightSpecular[8];
  vec4 lightPositionRotation[8]; // vec4(x, y, rotation, ???)
  vec4 lightNearFarAngleIntensity[8]; // vec4(near, far, angle, intensity)
};

// ** Varyings **
in vec4 v_channel_coords_01;
in vec4 v_channel_coords_23;
in vec4 v_uv_size; // Unscaled texture coordinates + Width / height of the objects
in vec3 v_screenCoordsRotation; // Where is this pixel on the screen?
in vec4 v_textureSizeTopLeft; // Actual size of the texture in pixels, it's position on the atlas.
in vec4 v_atlasSizeAsUV; // Actual size of the atlas in pixels, and it's relative size in UV coords.

// Variables
vec2 UV; // Unscaled texture coordinates
vec2 SIZE; // Width / height of the objects
vec4 CHANNEL_0; // Pixel value from texture channel 0
vec4 CHANNEL_1; // Pixel value from texture channel 1
vec4 CHANNEL_2; // Pixel value from texture channel 2
vec4 CHANNEL_3; // Pixel value from texture channel 3
vec2 CHANNEL_0_TEXTURE_COORDS; // Scaled texture coordinates
vec2 CHANNEL_1_TEXTURE_COORDS; // Scaled texture coordinates
vec2 CHANNEL_2_TEXTURE_COORDS; // Scaled texture coordinates
vec2 CHANNEL_3_TEXTURE_COORDS; // Scaled texture coordinates
vec2 CHANNEL_0_POSITION; // top left position of this texture on the atlas in UV coords
vec2 CHANNEL_0_SIZE; // size of this texture on the atlas in UV coords
vec2 SCREEN_COORDS;
float ROTATION;
vec2 TEXTURE_SIZE; // Size of the texture in pixels
vec2 ATLAS_SIZE; // Size of the atlas this texture is on, in pixels

int LIGHT_INDEX;
int LIGHT_COUNT;
int LIGHT_ACTIVE;
int LIGHT_TYPE;
int LIGHT_FAR_CUT_OFF;
int LIGHT_FALLOFF_TYPE;
vec4 LIGHT_COLOR;
vec4 LIGHT_SPECULAR;
vec2 LIGHT_POSITION;
float LIGHT_ROTATION;
float LIGHT_NEAR;
float LIGHT_FAR;
float LIGHT_ANGLE;
float LIGHT_INTENSITY;

// Constants
const float PI = 3.141592653589793;
const float PI_2 = PI * 0.5;
const float PI_4 = PI * 0.25;
const float TAU = 2.0 * PI;
const float TAU_2 = PI;
const float TAU_4 = PI_2;
const float TAU_8 = PI_4;

// Outputs
vec4 COLOR;

//#fragment_start
void fragment(){}
//#fragment_end

//#prepare_start
void prepare(){}
//#prepare_end

//#light_start
void light(){}
//#light_end

//#composite_start
void composite(){}
//#composite_end

void main(void) {
  // Defaults
  UV = v_uv_size.xy;
  SIZE = v_uv_size.zw;
  COLOR = vec4(0.0);
  CHANNEL_0_TEXTURE_COORDS = v_channel_coords_01.xy;
  CHANNEL_1_TEXTURE_COORDS = v_channel_coords_01.zw;
  CHANNEL_2_TEXTURE_COORDS = v_channel_coords_23.xy;
  CHANNEL_3_TEXTURE_COORDS = v_channel_coords_23.zw;
  CHANNEL_0 = texture(SRC_CHANNEL, CHANNEL_0_TEXTURE_COORDS);
  CHANNEL_1 = texture(SRC_CHANNEL, CHANNEL_1_TEXTURE_COORDS);
  CHANNEL_2 = texture(SRC_CHANNEL, CHANNEL_2_TEXTURE_COORDS);
  CHANNEL_3 = texture(SRC_CHANNEL, CHANNEL_3_TEXTURE_COORDS);
  SCREEN_COORDS = v_screenCoordsRotation.xy;
  ROTATION = v_screenCoordsRotation.z;
  TEXTURE_SIZE = v_textureSizeTopLeft.xy;
  ATLAS_SIZE = v_atlasSizeAsUV.xy;
  CHANNEL_0_POSITION = v_textureSizeTopLeft.zw;
  CHANNEL_0_SIZE = v_atlasSizeAsUV.zw;

  // Colour - build up the COLOR
  fragment();

  // Lighting - prepare, light, composite
  prepare();

  LIGHT_COUNT = min(8, max(0, int(round(numOfLights))));
  
  for(int i = 0; i < LIGHT_COUNT; i++) {
    LIGHT_INDEX = i;
    LIGHT_ACTIVE = int(round(lightFlags[i].x));
    LIGHT_TYPE = int(round(lightFlags[i].y));
    LIGHT_FAR_CUT_OFF = int(round(lightFlags[i].z));
    LIGHT_FALLOFF_TYPE = int(round(lightFlags[i].w));
    LIGHT_COLOR = lightColor[i];
    LIGHT_SPECULAR = lightSpecular[i];
    LIGHT_POSITION = lightPositionRotation[i].xy;
    LIGHT_ROTATION = lightPositionRotation[i].z;
    LIGHT_NEAR = lightNearFarAngleIntensity[i].x;
    LIGHT_FAR = lightNearFarAngleIntensity[i].y;
    LIGHT_ANGLE = lightNearFarAngleIntensity[i].z;
    LIGHT_INTENSITY = lightNearFarAngleIntensity[i].w;

    light();
  }

  // Composite - COMBINE COLOR + Lighting into final pixel color.
  composite();
  
  fragColor = COLOR;
}
