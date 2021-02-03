#version 300 es

precision mediump float;

layout(location = 0) out vec4 fragColor;

// ** Uniforms **
// ----------------
// Per object batch / texture / shader
// ----------------

// Currently we only ever bind one texture at a time.
// The texture is however an atlas of textures, so in
// practice you can read many sub-textures at once.
// Could remove this limitation.
uniform sampler2D u_channel_0;

// So here we should be able to set flags and mod them out.
// Modding (rather than bitwise) will work for GLSL 300 & 100.
// uniform highp float u_flags;

// ----------------
// Per layer
// ----------------
uniform vec4 u_ambientLight;

// ----------------
// Per frame
// ----------------
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
// internal
//in float v_materialType; // 0 = no texture, 1 = diffuse, 2 = albedo, emissive, normal, specular
//in float v_isLit;
// in vec4 v_texcoordSpecularIsLitMatType;

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

//#light_start
void light(){}
//#light_end

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

  // Basic colour
  COLOR = CHANNEL_0;

  fragment();

  // Lighting
  int lightCount = min(16, max(0, u_numOfLights));
  for(int i = 0; i < lightCount; i++) {
    light();
  }
  
  fragColor = COLOR + (AMBIENT_LIGHT * LIGHT);//COLOR * (AMBIENT_LIGHT + LIGHT); // Should combine or... not?
}
