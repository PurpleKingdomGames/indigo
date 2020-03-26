#version 300 es

precision lowp float;

layout (location = 0) in vec4 a_verticesAndCoords; // a_vertices, a_texcoord

layout (std140) uniform DisplayObjectUBO {
  mat4 u_projection;
  vec2 u_translation;
  vec2 u_scale;
  vec2 u_frameTranslation;
  vec2 u_frameScale;
  float u_lightType;
  float u_lightAttenuation;
  vec2 u_lightPosition;
  vec3 u_lightColor;
  float u_lightRotation;
  float u_lightAngle;
  float u_lightHeight;
  float u_lightNear;
  float u_lightFar;
  float u_lightPower;
};

out vec2 v_texcoord;

out vec2 v_relativeScreenCoords;

out float v_lightType;
out float v_lightAttenuation;
out vec2 v_lightPosition;
out vec3 v_lightColor;
out float v_lightRotation;
out float v_lightAngle;
out float v_lightHeight;
out float v_lightNear;
out float v_lightFar;
out float v_lightPower;

mat4 translate2d(vec2 t){
    return mat4(1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                t.x, t.y, 0, 1
                );
}

mat4 scale2d(vec2 s){
    return mat4(s.x, 0, 0, 0,
                0, s.y, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
                );
}

vec2 scaleTextCoords(){
  mat4 transform = translate2d(u_frameTranslation) * scale2d(u_frameScale);
  return (transform * vec4(a_verticesAndCoords.z, a_verticesAndCoords.w, 1, 1)).xy;
}

void main(void) {

  vec4 vertices = vec4(a_verticesAndCoords.x, a_verticesAndCoords.y, 1.0, 1.0);

  vec2 moveToTopLeft = u_scale / 2.0;

  mat4 transform = translate2d(moveToTopLeft + u_translation) * scale2d(u_scale);

  gl_Position = u_projection * transform * vertices;

  v_texcoord = scaleTextCoords();

  v_lightType = u_lightType;
  v_lightAttenuation = u_lightAttenuation;
  v_lightPosition = u_lightPosition;
  v_lightColor = u_lightColor;
  v_lightRotation = u_lightRotation;
  v_lightAngle = u_lightAngle;
  v_lightHeight = u_lightHeight;
  v_lightNear = u_lightNear;
  v_lightFar = u_lightFar;
  v_lightPower = u_lightPower;

  vec2 position = v_texcoord * u_scale;
  v_relativeScreenCoords = vec2(position.x, u_scale.y - position.y);
}
