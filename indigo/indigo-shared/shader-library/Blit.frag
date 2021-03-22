#version 300 es

precision mediump float;

vec4 CHANNEL_0;
vec4 COLOR;

//<indigo-fragment>
void fragment(){
  COLOR = CHANNEL_0;
}
//</indigo-fragment>

float PI;
float PI_2;
vec2 SCREEN_COORDS;

vec4 LIGHT;
vec4 SPECULAR; //TODO: Separate specular out, You need to COLOR * LIGHT + SPECULAR.

//<indigo-light>
const float screenGamma = 2.2;

vec3 LIGHT_COLOR = vec3(0.0, 1.0, 0.0);
vec2 LIGHT_POSITION = vec2(50.0, 50.0);
float LIGHT_ATTENUATION = 150.0;
float LIGHT_POWER = 1.2;
float LIGHT_HEIGHT = 1.0;
float LIGHT_ROTATION = 0.0;
float LIGHT_NEAR = 0.0;
float LIGHT_FAR = 200.0;
float LIGHT_ANGLE = 45.0;

vec4 calculateLight(float lightAmount, vec3 lightDir, float specularPower, float shinyAmount, vec4 specularTexture, vec4 normalTexture) {
  float shininess = shinyAmount * ((specularTexture.r + specularTexture.g + specularTexture.b) / 3.0);

  // Normal
  vec3 normalFlipedY = vec3(normalTexture.r, 1.0 - normalTexture.g, normalTexture.b); // Flip Y
  vec3 normal = normalize((2.0f * normalFlipedY) - 1.0f); // Convert RGB 0 to 1, into -1 to 1

  vec3 halfVec = vec3(0.0, 0.0, 1.0);

  float lambertian = max(dot(normal, lightDir), 0.0);

  vec3 reflection = normalize(vec3(2.0 * lambertian) * (normal - lightDir));
  float specular = min(pow(dot(reflection, halfVec), shininess), lambertian) * specularPower;

  vec4 color = mix(vec4(LIGHT_COLOR, lightAmount), vec4(LIGHT_COLOR, 1.0), specular);
  vec4 colorGammaCorrected = pow(color, vec4(1.0 / screenGamma));

  return vec4(colorGammaCorrected.rgb, colorGammaCorrected.a);
}

vec4 calculatePointLight(vec4 specularTexture, vec4 normalTexture) {
  vec3 pixelPosition = vec3(SCREEN_COORDS, 1.0);
  vec3 lightPosition = vec3(LIGHT_POSITION, 1.0);
  float lightAmount = clamp(1.0 - (distance(pixelPosition, lightPosition) / LIGHT_ATTENUATION), 0.0, 1.0);
  float specularPower = lightAmount * LIGHT_POWER;
  lightAmount = lightAmount * lightAmount * LIGHT_HEIGHT * clamp(LIGHT_POWER, 0.0, 1.0);
  float shinyAmount = 5.0;
  vec3 lightDir = normalize(lightPosition - pixelPosition);

  return calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
}

vec4 calculateDirectionLight(vec4 specularTexture, vec4 normalTexture) {
  float lightAmount = clamp(LIGHT_HEIGHT, 0.0, 1.0) * clamp(LIGHT_POWER, 0.0, 1.0);
  float specularPower = LIGHT_POWER;
  float shinyAmount = 1.0;
  vec3 lightDir = normalize(vec3(sin(LIGHT_ROTATION), cos(LIGHT_ROTATION), 0.1));

  return calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
}

vec4 calculateSpotLight(vec4 specularTexture, vec4 normalTexture) {
  vec3 pixelPosition = vec3(SCREEN_COORDS, 1.0);
  vec3 lightPosition = vec3(LIGHT_POSITION, 1.0);
  float lightAmount = clamp(1.0 - (distance(pixelPosition, lightPosition) / LIGHT_ATTENUATION), 0.0, 1.0);
  float specularPower = lightAmount * LIGHT_POWER;
  lightAmount = lightAmount * lightAmount * LIGHT_HEIGHT * clamp(LIGHT_POWER, 0.0, 1.0);
  float shinyAmount = 5.0;
  vec3 lightDir = normalize(lightPosition - pixelPosition);

  float near = LIGHT_NEAR;
  float far = LIGHT_FAR;
  float viewingAngle = LIGHT_ANGLE;
  float viewingAngleBy2 = viewingAngle / 2.0;

  float distanceToLight = distance(SCREEN_COORDS, LIGHT_POSITION);

  vec4 finalColor = vec4(0.0);

  if(distanceToLight > near && distanceToLight < far) {

    vec2 lookAtRelativeToLight = vec2(sin(LIGHT_ROTATION), cos(LIGHT_ROTATION));
    float angleToLookAt = atan(lookAtRelativeToLight.y, lookAtRelativeToLight.x) + PI;
    float anglePlus = mod(angleToLookAt + viewingAngleBy2, 2.0 * PI);
    float angleMinus = mod(angleToLookAt - viewingAngleBy2, 2.0 * PI);

    vec2 pixelRelativeToLight = SCREEN_COORDS - LIGHT_POSITION;
    float angleToPixel = atan(pixelRelativeToLight.y, pixelRelativeToLight.x) + PI;

    if(anglePlus < angleMinus && (angleToPixel < anglePlus || angleToPixel > angleMinus)) {
      finalColor = calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
    }

    if(anglePlus > angleMinus && (angleToPixel < anglePlus && angleToPixel > angleMinus)) {
      finalColor = calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
    }

  }

  return finalColor;
}


void light(){
  LIGHT = vec4(1.0, 0.0, 0.0, 1.0);
  SPECULAR = vec4(0.0);
}
//</indigo-light>
