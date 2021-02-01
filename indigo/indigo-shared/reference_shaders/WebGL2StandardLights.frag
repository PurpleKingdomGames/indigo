#version 300 es

precision lowp float;

in vec2 v_texcoord;

in vec2 v_relativeScreenCoords;

in float v_lightType;
in float v_lightAttenuation;
in vec2 v_lightPosition;
in vec3 v_lightColor;
in float v_lightRotation;
in float v_lightAngle;
in float v_lightHeight;
in float v_lightNear;
in float v_lightFar;
in float v_lightPower;

uniform sampler2D u_texture_game_normal;
uniform sampler2D u_texture_game_specular;

out vec4 fragColor;

const float screenGamma = 2.2;

const float PI = 3.1415926535897932384626433832795;
const float PI_2 = 1.57079632679489661923;
const float PI_4 = 0.785398163397448309616;

vec4 calculateLight(float lightAmount, vec3 lightDir, float specularPower, float shinyAmount, vec4 specularTexture, vec4 normalTexture) {
  float shininess = shinyAmount * ((specularTexture.r + specularTexture.g + specularTexture.b) / 3.0);

  // Normal
  vec3 normalFlipedY = vec3(normalTexture.r, 1.0 - normalTexture.g, normalTexture.b); // Flip Y
  vec3 normal = normalize((2.0f * normalFlipedY) - 1.0f); // Convert RGB 0 to 1, into -1 to 1

  vec3 halfVec = vec3(0.0, 0.0, 1.0);

  float lambertian = max(dot(normal, lightDir), 0.0);

  vec3 reflection = normalize(vec3(2.0 * lambertian) * (normal - lightDir));
  float specular = min(pow(dot(reflection, halfVec), shininess), lambertian) * specularPower;

  vec4 color = mix(vec4(v_lightColor, lightAmount), vec4(v_lightColor, 1.0), specular);
  vec4 colorGammaCorrected = pow(color, vec4(1.0 / screenGamma));

  return vec4(colorGammaCorrected.rgb, colorGammaCorrected.a);
}

vec4 calculatePointLight(vec4 specularTexture, vec4 normalTexture) {
  vec3 pixelPosition = vec3(v_relativeScreenCoords, 1.0);
  vec3 lightPosition = vec3(v_lightPosition, 1.0);
  float lightAmount = clamp(1.0 - (distance(pixelPosition, lightPosition) / v_lightAttenuation), 0.0, 1.0);
  float specularPower = lightAmount * v_lightPower;
  lightAmount = lightAmount * lightAmount * v_lightHeight * clamp(v_lightPower, 0.0, 1.0);
  float shinyAmount = 5.0;
  vec3 lightDir = normalize(lightPosition - pixelPosition);

  return calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
}

vec4 calculateDirectionLight(vec4 specularTexture, vec4 normalTexture) {
  float lightAmount = clamp(v_lightHeight, 0.0, 1.0) * clamp(v_lightPower, 0.0, 1.0);
  float specularPower = v_lightPower;
  float shinyAmount = 1.0;
  vec3 lightDir = normalize(vec3(sin(v_lightRotation), cos(v_lightRotation), 0.1));

  return calculateLight(lightAmount, lightDir, specularPower, shinyAmount, specularTexture, normalTexture);
}

vec4 calculateSpotLight(vec4 specularTexture, vec4 normalTexture) {
  vec3 pixelPosition = vec3(v_relativeScreenCoords, 1.0);
  vec3 lightPosition = vec3(v_lightPosition, 1.0);
  float lightAmount = clamp(1.0 - (distance(pixelPosition, lightPosition) / v_lightAttenuation), 0.0, 1.0);
  float specularPower = lightAmount * v_lightPower;
  lightAmount = lightAmount * lightAmount * v_lightHeight * clamp(v_lightPower, 0.0, 1.0);
  float shinyAmount = 5.0;
  vec3 lightDir = normalize(lightPosition - pixelPosition);

  float near = v_lightNear;
  float far = v_lightFar;
  float viewingAngle = v_lightAngle;
  float viewingAngleBy2 = viewingAngle / 2.0;

  float distanceToLight = distance(v_relativeScreenCoords, v_lightPosition);

  vec4 finalColor = vec4(0.0);

  if(distanceToLight > near && distanceToLight < far) {

    vec2 lookAtRelativeToLight = vec2(sin(v_lightRotation), cos(v_lightRotation));
    float angleToLookAt = atan(lookAtRelativeToLight.y, lookAtRelativeToLight.x) + PI;
    float anglePlus = mod(angleToLookAt + viewingAngleBy2, 2.0 * PI);
    float angleMinus = mod(angleToLookAt - viewingAngleBy2, 2.0 * PI);

    vec2 pixelRelativeToLight = v_relativeScreenCoords - v_lightPosition;
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

void main(void) {

  vec4 specularTexture = texture(u_texture_game_specular, v_texcoord);
  vec4 normalTexture = texture(u_texture_game_normal, v_texcoord);

  vec4 lightColor = vec4(0.0);

  if(v_lightType == 1.0) {
    lightColor = calculatePointLight(specularTexture, normalTexture);
  }

  if(v_lightType == 2.0) {
    lightColor = calculateDirectionLight(specularTexture, normalTexture);
  }

  if(v_lightType == 3.0) {
    lightColor = calculateSpotLight(specularTexture, normalTexture);
  }

  if(normalTexture.rgb == vec3(0.0)) {
    lightColor = vec4(0.0);
  }

  fragColor = lightColor;
}
