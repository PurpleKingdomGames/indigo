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
uniform float TIME;
uniform float TIME_DELTA;
uniform float WINDOW_SIZE;

// Could be UBO?
uniform int u_numOfLights; // max 16
uniform int u_lightType[16];
uniform float u_lightAttenuation[16];
uniform vec2 u_lightPosition[16];
uniform vec3 u_lightColor[16];
uniform float u_lightRotation[16];
uniform float u_lightAngle[16];
uniform float u_lightHeight[16];
uniform float u_lightNear[16];
uniform float u_lightFar[16];
uniform float u_lightPower[16];

// ** Varyings **
// internal
in vec2 v_texcoords;
in float v_materialType; // 0 = no texture, 1 = diffuse, 2 = albedo, emissive, normal, specular
in float v_isLit;

// public
in vec2 UV;
in vec2 SIZE;
in vec2 POSITION;

// Outputs
vec4 COLOR;
vec4 LIGHT;
vec4 AMBIENT_LIGHT;

void fragment(){}

void light(){}

void main(void) {
  COLOR = texture(u_texture, v_texcoords);
  AMBIENT_LIGHT = u_ambientLight;

  fragment();

  int lightCount = min(16, max(0, u_numOfLights));
  for(int i = 0; i < lightCount; i++) {
    LIGHT = vec4(0.0);
    //TODO calculate light information
    light();
  }
  
  fragColor = COLOR + (AMBIENT_LIGHT * LIGHT);
}
