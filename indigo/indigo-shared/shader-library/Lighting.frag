#version 300 es

precision mediump float;

vec4 CHANNEL_0; // Diffuse / Albedo
vec4 CHANNEL_1; // Emissive
vec4 CHANNEL_2; // Normal
vec4 CHANNEL_3; // Specular
vec4 COLOR;

float TAU;
float PI;
float PI_2;
float PI_4;
vec2 SCREEN_COORDS;
float TIME;
float ROTATION;

int LIGHT_COUNT;
int LIGHT_ACTIVE;
int LIGHT_TYPE;
vec3 LIGHT_COLOR;
float LIGHT_POWER;
vec3 LIGHT_SPECULAR_COLOR;
float LIGHT_SPECULAR_POWER;
vec2 LIGHT_POSITION;
float LIGHT_HEIGHT;
float LIGHT_ROTATION;
float LIGHT_NEAR;
float LIGHT_FAR;
float LIGHT_ANGLE;
float LIGHT_ATTENUATION;

//<indigo-prepare>
const float SCREEN_GAMMA = 2.2;

layout (std140) uniform IndigoMaterialLightingData {
  highp vec2 LIGHT_EMISSIVE; // vec2(set?, amount)
  highp vec2 LIGHT_NORMAL; // vec2(set?, amount)
  highp vec2 LIGHT_ROUGHNESS; // vec2(set?, amount)
};

vec4 normalColor;
vec4 roughnessColor;
vec4 emissiveColor;
vec4 lightAcc;
vec4 specularAcc;

mat4 rotationZ(in float angle) {
  return mat4(cos(angle),  -sin(angle), 0, 0,
              sin(angle),  cos(angle),  0, 0,
              0,           0,           1, 0,
              0,           0,           0, 1);
}

void calculateLight(in float lightAmount,
                    in vec3 lightDir,
                    in float specularPower,
                    in vec4 normalTexture,
                    in vec4 specularTexture,
                    out vec4 outColor,
                    out vec4 outSpecular) {
  float shininess = (specularTexture.r + specularTexture.g + specularTexture.b) / 3.0;

  // Normal - Convert RGB 0 to 1, into -1 to 1
  vec3 normal = normalize((2.0f * vec3(normalTexture.rg, 1.0)) - 1.0f);
  vec3 rotatedNormal = (vec4(normal, 1.0) * rotationZ(ROTATION)).xyz;

  vec3 halfVec = vec3(0.0, 0.0, 1.0);
  float lambertian = max(-dot(rotatedNormal, lightDir), 0.0);
  vec3 reflection = normalize(vec3(2.0 * lambertian) * (rotatedNormal - lightDir));
  float specular = min(pow(dot(reflection, halfVec), shininess), lambertian) * specularPower;
  vec4 color = vec4(LIGHT_COLOR.rgb * lightAmount, lightAmount);
  vec4 colorGammaCorrected = pow(color, vec4(1.0 / SCREEN_GAMMA));

  outColor = colorGammaCorrected;
  outSpecular = vec4(LIGHT_SPECULAR_COLOR * specular, specular);
}

void calculateAmbientLight(out vec4 outColor) {
  outColor = vec4(LIGHT_COLOR * LIGHT_POWER, LIGHT_POWER);
}

void calculateDirectionLight(vec4 normalTexture, vec4 specularTexture, out vec4 outColor, out vec4 outSpecular) {
  float lightAmount = clamp(LIGHT_HEIGHT, 0.0, 1.0) * clamp(LIGHT_POWER, 0.0, 1.0);
  vec3 lightDir = normalize(vec3(sin(LIGHT_ROTATION), cos(LIGHT_ROTATION), 0.0));
  float specularPower = LIGHT_SPECULAR_POWER;

  calculateLight(lightAmount, lightDir, specularPower, normalTexture, specularTexture, outColor, outSpecular);
}

void calculatePointLight(vec4 normalTexture, vec4 specularTexture, out vec4 outColor, out vec4 outSpecular) {
  // LIGHT_COUNT = LIGHT_COUNT;
  // LIGHT_ACTIVE = LIGHT_ACTIVE;
  // LIGHT_TYPE = LIGHT_TYPE;
  // LIGHT_COLOR = vec3(1.0);
  // LIGHT_POWER = 1.0;
  // LIGHT_SPECULAR_COLOR = vec3(1.0);
  // LIGHT_SPECULAR_POWER = 3.0;
  // LIGHT_POSITION = vec2(0.0, 0.0);
  // LIGHT_HEIGHT = 1.0;
  // LIGHT_ROTATION = 0.0;
  // LIGHT_NEAR = 0.0;
  // LIGHT_FAR = 20.0;
  // LIGHT_ANGLE = 0.0;
  LIGHT_ATTENUATION = 0.075;

  vec3 pixelPosition = vec3(SCREEN_COORDS, 0.0);
  vec3 lightPosition = vec3(LIGHT_POSITION, 0.0);
  float lightAmount = clamp(1.0 - (distance(pixelPosition, lightPosition) / LIGHT_ATTENUATION), 0.0, 1.0);
  float specularPower = lightAmount * LIGHT_SPECULAR_POWER;
  lightAmount = lightAmount * lightAmount * LIGHT_HEIGHT * clamp(LIGHT_POWER, 0.0, 1.0);
  vec3 lightDir = normalize(lightPosition - pixelPosition);

  calculateLight(lightAmount, lightDir, specularPower, normalTexture, specularTexture, outColor, outSpecular);
}

void prepare(){

  // Texture order: albedo, emissive, normal, roughness

  // Initialise values
  lightAcc = vec4(0.0, 0.0, 0.0, 1.0);
  specularAcc = vec4(0.0);
  emissiveColor = vec4(0.0, 0.0, 0.0, 1.0);
  normalColor = vec4(0.5, 0.5, 1.0, 1.0);
  roughnessColor = vec4(0.0, 0.0, 0.0, 1.0);

  if(LIGHT_EMISSIVE.x > 0.0) {
    emissiveColor = mix(emissiveColor, CHANNEL_1, CHANNEL_1.a * LIGHT_EMISSIVE.y);
  }

  if(LIGHT_NORMAL.x > 0.0) {
    normalColor = mix(normalColor, CHANNEL_2, CHANNEL_2.a * LIGHT_NORMAL.y);
  }

  if(LIGHT_ROUGHNESS.x > 0.0) {
    roughnessColor = mix(roughnessColor, CHANNEL_3, CHANNEL_3.a * LIGHT_ROUGHNESS.y);
  }

}
//</indigo-prepare>

//<indigo-light>

void light(){

  if(LIGHT_ACTIVE == 1) { // light is active

    vec4 lightResult = vec4(0.0);
    vec4 specularResult = vec4(0.0);

    // 0 = ambient, 1 = direction, 2 = point, 3 = spot
    switch(LIGHT_TYPE) {
      case 0:
        calculateAmbientLight(lightResult);
        break;

      case 1:
        calculateDirectionLight(normalColor, roughnessColor, lightResult, specularResult);
        break;

      case 2:
        calculatePointLight(normalColor, roughnessColor, lightResult, specularResult);
        break;

      case 3:
        //
        break;

      default:
        //
        break;
    }

    specularAcc = specularAcc + specularResult;
    lightAcc = lightAcc + lightResult;
  }
}
//</indigo-light>


//<indigo-composite>
void composite() {
  float emmisiveAlpha = clamp(emissiveColor.r + emissiveColor.g + emissiveColor.b, 0.0, 1.0);
  vec4 emissiveResult = vec4(emissiveColor.rgb * emmisiveAlpha, emmisiveAlpha);

  vec4 colorLightSpec = mix(COLOR * lightAcc, specularAcc, specularAcc.a * COLOR.a);

  COLOR = mix(colorLightSpec, emissiveResult, emissiveResult.a);
}
//</indigo-composite>


// vec4 calculatePointLight(vec4 specularTexture, vec4 normalTexture) {
//   vec3 pixelPosition = vec3(SCREEN_COORDS, 1.0);
//   vec3 lightPosition = vec3(LIGHT_POSITION, 1.0);
//   float lightAmount = clamp(1.0 - (distance(pixelPosition, lightPosition) / LIGHT_ATTENUATION), 0.0, 1.0);
//   float specularPower = lightAmount * LIGHT_RGB_POWER.a;
//   lightAmount = lightAmount * lightAmount * LIGHT_HEIGHT * clamp(LIGHT_RGB_POWER.a, 0.0, 1.0);
//   float shinyAmount = 5.0;
//   vec3 lightDir = normalize(lightPosition - pixelPosition);

//   return calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
// }

// vec4 calculateSpotLight(vec4 specularTexture, vec4 normalTexture) {
//   vec3 pixelPosition = vec3(SCREEN_COORDS, 1.0);
//   vec3 lightPosition = vec3(LIGHT_POSITION, 1.0);
//   float lightAmount = clamp(1.0 - (distance(pixelPosition, lightPosition) / LIGHT_ATTENUATION), 0.0, 1.0);
//   float specularPower = lightAmount * LIGHT_RGB_POWER.a;
//   lightAmount = lightAmount * lightAmount * LIGHT_HEIGHT * clamp(LIGHT_RGB_POWER.a, 0.0, 1.0);
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
