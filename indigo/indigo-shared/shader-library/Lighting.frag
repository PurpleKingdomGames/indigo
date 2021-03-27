#version 300 es

precision mediump float;

vec4 CHANNEL_0; // Diffuse / Albedo
vec4 CHANNEL_1; // Emissive
vec4 CHANNEL_2; // Normal
vec4 CHANNEL_3; // Specular
vec4 COLOR;
vec4 SPECULAR;
vec4 LIGHT;

float TAU;
float PI;
float PI_2;
float PI_4;
vec2 SCREEN_COORDS;
float TIME;
float ROTATION;

//<indigo-light>
const float screenGamma = 2.2;

vec3 LIGHT_COLOR = vec3(0.0, 1.0, 0.0);
vec3 LIGHT_SPECULAR_COLOR = vec3(1.0, 1.0, 0.0);
vec2 LIGHT_POSITION = vec2(0.0, 0.0);
float LIGHT_ATTENUATION = 150.0;
float LIGHT_POWER = 1.0;
float LIGHT_HEIGHT = 0.25;
float LIGHT_ROTATION = 0.0;
float LIGHT_NEAR = 0.0;
float LIGHT_FAR = 200.0;
float LIGHT_ANGLE = 45.0;

mat4 rotationZ(in float angle) {
  return mat4(cos(angle),  -sin(angle), 0, 0,
              sin(angle),  cos(angle),  0, 0,
              0,           0,           1, 0,
              0,           0,           0, 1);
}

void calculateLight(in float lightAmount,
                    in vec3 lightDir,
                    in float specularPower,
                    in float shinyAmount,
                    in vec4 specularTexture,
                    in vec4 normalTexture,
                    out vec4 outColor,
                    out vec4 outSpecular) {
  float shininess = shinyAmount * ((specularTexture.r + specularTexture.g + specularTexture.b) / 3.0);

  // Normal
  // Convert RGB 0 to 1, into -1 to 1
  vec3 normal = normalize((2.0f * vec3(normalTexture.rg, 1.0)) - 1.0f);
  vec3 rotatedNormal = (vec4(normal, 1.0) * rotationZ(ROTATION)).xyz;

  vec3 halfVec = vec3(0.0, 0.0, 1.0);

  float lambertian = max(-dot(rotatedNormal, lightDir), 0.0);

  vec3 reflection = normalize(vec3(2.0 * lambertian) * (rotatedNormal - lightDir));
  float specular = min(pow(dot(reflection, halfVec), shininess), lambertian) * specularPower;

  vec4 color = vec4(LIGHT_COLOR * lightAmount, lightAmount);
  vec4 colorGammaCorrected = pow(color, vec4(1.0 / screenGamma));

  outColor = colorGammaCorrected;
  outSpecular = vec4(LIGHT_SPECULAR_COLOR * specular, specular);
}

void calculateDirectionLight(vec4 specularTexture, vec4 normalTexture, out vec4 outColor, out vec4 outSpecular) {
  float lightAmount = clamp(LIGHT_HEIGHT, 0.0, 1.0) * clamp(LIGHT_POWER, 0.0, 1.0);
  float specularPower = LIGHT_POWER;
  float shinyAmount = 1.0;
  vec3 lightDir = normalize(vec3(sin(LIGHT_ROTATION), cos(LIGHT_ROTATION), 0.0));

  calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture, outColor, outSpecular);
}

float timeToRadians(float t) {
  return TAU * mod(t * 0.5, 1.0);
}

void light(){

  // TODO: Ignore / do no work on any light that is too far away.

  LIGHT_ROTATION = timeToRadians(TIME);

  // Texture order: albedo, emissive, normal, specular

  float NORMAL_AMOUNT = 1.0; // TODO: Should come from UBO?
  float SPECULAR_AMOUNT = 1.0; // TODO: Should come from UBO?

  vec4 normal = vec4(0.5, 0.5, 1.0, 1.0);
  vec4 specular = vec4(0.0, 0.0, 0.0, 1.0);

  if(NORMAL_AMOUNT > 0.0) {
    normal = mix(normal, CHANNEL_2, CHANNEL_2.a);
  }

  if(SPECULAR_AMOUNT > 0.0) {
    specular = mix(specular, CHANNEL_3, CHANNEL_3.a);
  }

  vec4 lightResult = vec4(0.0);
  vec4 specularResult = vec4(0.0);

  calculateDirectionLight(specular, normal, lightResult, specularResult);

  SPECULAR = SPECULAR + specularResult;
  LIGHT = LIGHT + lightResult;
}
//</indigo-light>


//<indigo-composite>
void composite() {
  float EMISSIVE_AMOUNT = 1.0;

  vec4 emissive = vec4(0.0, 0.0, 0.0, 1.0);

  if(EMISSIVE_AMOUNT > 0.0) {
    emissive = mix(emissive, CHANNEL_1, CHANNEL_1.a);
  }

  float emmisiveAlpha = clamp(emissive.r + emissive.g + emissive.b, 0.0, 1.0);
  vec4 emissiveResult = vec4(emissive.rgb * emmisiveAlpha, emmisiveAlpha);

  vec4 colorLightSpec = mix(COLOR * LIGHT, SPECULAR, SPECULAR.a * COLOR.a);

  COLOR = mix(colorLightSpec, emissiveResult, emissiveResult.a);
}
//</indigo-composite>


// vec4 calculatePointLight(vec4 specularTexture, vec4 normalTexture) {
//   vec3 pixelPosition = vec3(SCREEN_COORDS, 1.0);
//   vec3 lightPosition = vec3(LIGHT_POSITION, 1.0);
//   float lightAmount = clamp(1.0 - (distance(pixelPosition, lightPosition) / LIGHT_ATTENUATION), 0.0, 1.0);
//   float specularPower = lightAmount * LIGHT_POWER;
//   lightAmount = lightAmount * lightAmount * LIGHT_HEIGHT * clamp(LIGHT_POWER, 0.0, 1.0);
//   float shinyAmount = 5.0;
//   vec3 lightDir = normalize(lightPosition - pixelPosition);

//   return calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
// }

// vec4 calculateSpotLight(vec4 specularTexture, vec4 normalTexture) {
//   vec3 pixelPosition = vec3(SCREEN_COORDS, 1.0);
//   vec3 lightPosition = vec3(LIGHT_POSITION, 1.0);
//   float lightAmount = clamp(1.0 - (distance(pixelPosition, lightPosition) / LIGHT_ATTENUATION), 0.0, 1.0);
//   float specularPower = lightAmount * LIGHT_POWER;
//   lightAmount = lightAmount * lightAmount * LIGHT_HEIGHT * clamp(LIGHT_POWER, 0.0, 1.0);
//   float shinyAmount = 5.0;
//   vec3 lightDir = normalize(lightPosition - pixelPosition);

//   float near = LIGHT_NEAR;
//   float far = LIGHT_FAR;
//   float viewingAngle = LIGHT_ANGLE;
//   float viewingAngleBy2 = viewingAngle / 2.0;

//   float distanceToLight = distance(SCREEN_COORDS, LIGHT_POSITION);

//   vec4 finalColor = vec4(0.0);

//   if(distanceToLight > near && distanceToLight < far) {

//     vec2 lookAtRelativeToLight = vec2(sin(LIGHT_ROTATION), cos(LIGHT_ROTATION));
//     float angleToLookAt = atan(lookAtRelativeToLight.y, lookAtRelativeToLight.x) + PI;
//     float anglePlus = mod(angleToLookAt + viewingAngleBy2, 2.0 * PI);
//     float angleMinus = mod(angleToLookAt - viewingAngleBy2, 2.0 * PI);

//     vec2 pixelRelativeToLight = SCREEN_COORDS - LIGHT_POSITION;
//     float angleToPixel = atan(pixelRelativeToLight.y, pixelRelativeToLight.x) + PI;

//     if(anglePlus < angleMinus && (angleToPixel < anglePlus || angleToPixel > angleMinus)) {
//       finalColor = calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
//     }

//     if(anglePlus > angleMinus && (angleToPixel < anglePlus && angleToPixel > angleMinus)) {
//       finalColor = calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
//     }

//   }

//   return finalColor;
// }
