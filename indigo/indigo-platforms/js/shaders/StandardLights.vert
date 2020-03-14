#version 300 es

precision lowp float;

layout (location = 0) in vec4 a_verticesAndCoords; // a_vertices, a_texcoord

layout (std140) uniform DisplayObjectUBO {
  mat4 u_projection;
  vec2 u_translation;
  vec2 u_scale;
  vec2 u_frameTranslation;
  vec2 u_frameScale;
};

out vec2 v_texcoord;

out vec2[1] v_lights;
out vec2 v_relativeScreenCoords;

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

  float centerX = (228.0 * 3.0) / 2.0; // TODO: Remove when real values passed in.
  float centerY = (128.0 * 3.0) / 2.0; // TODO: Remove when real values passed in.

  v_lights = vec2[1](vec2(centerX - 50.0, centerY - 30.0));
  vec2 position = v_texcoord * u_scale;
  v_relativeScreenCoords = vec2(position.x, u_scale.y - position.y);
}
