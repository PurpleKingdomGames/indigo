#version 300 es

precision lowp float;

layout(location = 0) out vec4 fragColor;

// ** Uniforms **
// ----------------
// Per object batch
// ----------------
uniform sampler2D u_texture;

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
in vec4 v_texcoordEmissiveNormal;
in vec4 v_textureAmounts;
in vec4 v_texcoordSpecularIsLitMatType;

// public
in vec2 TEXCOORDS;
in vec2 UV; // Unscaled texture coordinates
in vec2 SIZE; // Width / height of the objects
in vec2 POSITION; // Position on the screen.
in float ALPHA; // Alpha of entity

// Outputs
vec4 COLOR;
vec4 LIGHT;
vec4 AMBIENT_LIGHT;

void fragment(){}

void light(){}

void main(void) {
  // Basic colour
  COLOR = texture(u_texture, TEXCOORDS);
  COLOR = vec4(COLOR.rgb, COLOR.a * ALPHA);

  fragment();

  // Lighting
  AMBIENT_LIGHT = u_ambientLight;

  float albedoAmount = v_textureAmounts.x;
  float emissiveAmount = v_textureAmounts.y;
  float normalAmount = v_textureAmounts.z;
  float specularAmount = v_textureAmounts.w;

  int lightCount = min(16, max(0, u_numOfLights));
  for(int i = 0; i < lightCount; i++) {
    LIGHT = vec4(0.0);
    //TODO calculate light information
    light();
  }
  
  fragColor = COLOR + (AMBIENT_LIGHT * LIGHT);
}
