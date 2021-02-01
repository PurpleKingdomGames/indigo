#version 300 es

precision mediump float;

layout(location = 0) out vec4 fragColor;

// ** Uniforms **
// ----------------
// Per object batch / texture / shader
// ----------------
uniform sampler2D u_texture;
// uniform sampler2D CHANNEL_1;
// uniform sampler2D CHANNEL_2;
// uniform sampler2D CHANNEL_3;
// uniform sampler2D CHANNEL_4;

// So here we should be able to set flags and mod them out.
// Modding (rather than bitwise) will work for GLSL 300 & 100
// and provde up to 23 flags.
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
uniform float TIME_DELTA; // Time delta between frames
uniform float WINDOW_SIZE; // Size of the viewport

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
// in vec4 v_texcoordEmissiveNormal;
// in vec4 v_textureAmounts;
// in vec4 v_texcoordSpecularIsLitMatType;

// public
in vec2 TEXCOORDS;
in vec2 UV; // Unscaled texture coordinates
in vec2 SIZE; // Width / height of the objects
in float ALPHA; // Alpha of entity

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
  COLOR = vec4(0.0);
  LIGHT = vec4(0.0);
  AMBIENT_LIGHT = u_ambientLight;

  // Basic colour
  COLOR = texture(u_texture, TEXCOORDS);
  COLOR = vec4(COLOR.rgb, COLOR.a * ALPHA);

  fragment();

  // Lighting

  // float albedoAmount = v_textureAmounts.x;
  // float emissiveAmount = v_textureAmounts.y;
  // float normalAmount = v_textureAmounts.z;
  // float specularAmount = v_textureAmounts.w;

  int lightCount = min(16, max(0, u_numOfLights));
  for(int i = 0; i < lightCount; i++) {
    light();
  }
  
  fragColor = COLOR * (AMBIENT_LIGHT + LIGHT); // Should combine or... not?
}
