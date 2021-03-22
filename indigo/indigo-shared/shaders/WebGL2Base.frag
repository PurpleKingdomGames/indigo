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

// Could be UBO?
// Can be packed up.
uniform int u_numOfLights; // max 16
uniform int u_lightType[16];
uniform vec3 u_lightColor[16];
uniform vec2 u_lightPosition[16];
uniform float u_lightRotation[16];
uniform float u_lightAngle[16];
uniform float u_lightHeight[16];
uniform float u_lightNear[16];
uniform float u_lightFar[16];
uniform float u_lightPower[16];
uniform float u_lightAttenuation[16];

// ** Varyings **
in vec4 v_channel_coords_01;
in vec4 v_channel_coords_23;
in vec4 v_uv_size; // Unscaled texture coordinates + Width / height of the objects
in vec2 v_screenCoords; // Where is this pixel on the screen?

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
vec2 SCREEN_COORDS;

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
vec4 LIGHT;
vec4 SPECULAR;

//#fragment_start
void fragment(){}
//#fragment_end

//#light_start
void light(){}
//#light_end

void main(void) {
  // Defaults
  UV = v_uv_size.xy;
  SIZE = v_uv_size.zw;
  COLOR = vec4(0.0);
  LIGHT = vec4(1.0);
  SPECULAR = vec4(0.0);
  CHANNEL_0_TEXTURE_COORDS = v_channel_coords_01.xy;
  CHANNEL_1_TEXTURE_COORDS = v_channel_coords_01.zw;
  CHANNEL_2_TEXTURE_COORDS = v_channel_coords_23.xy;
  CHANNEL_3_TEXTURE_COORDS = v_channel_coords_23.zw;
  CHANNEL_0 = texture(SRC_CHANNEL, CHANNEL_0_TEXTURE_COORDS);
  CHANNEL_1 = texture(SRC_CHANNEL, CHANNEL_1_TEXTURE_COORDS);
  CHANNEL_2 = texture(SRC_CHANNEL, CHANNEL_2_TEXTURE_COORDS);
  CHANNEL_3 = texture(SRC_CHANNEL, CHANNEL_3_TEXTURE_COORDS);
  SCREEN_COORDS = v_screenCoords;

  // Colour
  fragment();

  // Lighting
  // int lightCount = min(16, max(0, u_numOfLights));
  int lightCount = 1; // TODO: Remove! Tmp, while testing lights
  for(int i = 0; i < lightCount; i++) {
    light();
  }
  
  vec4 specular = vec4(SPECULAR.rgb, SPECULAR.a * COLOR.a);
  fragColor = mix(COLOR * LIGHT, specular, specular.a);
}
