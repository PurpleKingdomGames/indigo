#version 300 es

precision mediump float;

layout(location = 0) out vec4 fragColor;

// ** Uniforms **
// Currently we only ever bind one texture at a time.
// The texture is however an atlas of textures, so in
// practice you can read many sub-textures at once.
// Could remove this limitation.
uniform sampler2D u_channel_0;
uniform vec4 u_ambientLight;
// public
uniform float TIME; // Running time

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

// Variables
vec2 UV; // Unscaled texture coordinates
vec2 SIZE; // Width / height of the objects
vec4 CHANNEL_0; // Pixel value from texture channel 0
vec4 CHANNEL_1; // Pixel value from texture channel 1
vec4 CHANNEL_2; // Pixel value from texture channel 2
vec4 CHANNEL_3; // Pixel value from texture channel 3

// Constants
const float TAU = 2.0 * 3.141592653589793;
const float PI = 3.141592653589793;

// Outputs
vec4 COLOR;
vec4 LIGHT;
vec4 AMBIENT_LIGHT;

//#fragment_start
void fragment(){}
//#fragment_end

//#post_fragment_start
void postFragment(){}
//#post_fragment_end

//#light_start
void light(){}
//#light_end

//#post_light_start
void postLight(){}
//#post_light_end

void main(void) {
  // Defaults
  UV = v_uv_size.xy;
  SIZE = v_uv_size.zw;
  COLOR = vec4(0.0);
  LIGHT = vec4(0.0);
  AMBIENT_LIGHT = u_ambientLight;
  CHANNEL_0 = texture(u_channel_0, v_channel_coords_01.xy);
  CHANNEL_1 = texture(u_channel_0, v_channel_coords_01.zw);
  CHANNEL_2 = texture(u_channel_0, v_channel_coords_23.xy);
  CHANNEL_3 = texture(u_channel_0, v_channel_coords_23.zw);

  // Colour
  fragment();
  postFragment();

  // Lighting
  int lightCount = min(16, max(0, u_numOfLights));
  for(int i = 0; i < lightCount; i++) {
    light();
    postLight();
  }
  
  fragColor = COLOR + (AMBIENT_LIGHT * LIGHT);
}
