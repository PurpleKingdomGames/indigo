#version 300 es

precision lowp float;

layout (location = 0) in vec4 a_verticesAndCoords; // a_vertices, a_texcoord
layout (location = 1) in vec4 a_matRotateScale; // mat(0,1,4,5)
layout (location = 2) in vec4 a_matTranslateAlpha; // mat(12,13,14,alpha)
layout (location = 3) in vec2 a_size; // 
layout (location = 4) in vec4 a_frameTransform; // a_frameTranslation, a_frameScale

uniform mat4 u_projection;

out vec2 v_texcoords;

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

vec2 scaleTexCoordsWithOffset(vec2 texcoord, vec2 offset){
  mat4 transform = translate2d(offset) * scale2d(a_frameTransform.zw);
  return (transform * vec4(texcoord, 1.0, 1.0)).xy;
}

vec2 scaleTexCoords(vec2 texcoord){
  return scaleTexCoordsWithOffset(texcoord, a_frameTransform.xy);
}

void main(void) {

  vec4 vertices = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
  vec2 texcoords = scaleTexCoords(a_verticesAndCoords.zw);

  mat4 transform =
    mat4(a_matRotateScale.x,    a_matRotateScale.y,    0,                     0,
         a_matRotateScale.z,    a_matRotateScale.w,    0,                     0,
         0,                     0,                     1,                     0,
         a_matTranslateAlpha.x, a_matTranslateAlpha.y, a_matTranslateAlpha.z, 1
        );

  gl_Position = u_projection * transform * vertices;

  v_texcoords = texcoords;

}
