#version 300 es

// Attributes
in vec4 a_vertices;
in vec2 a_texcoord;

// Uniforms
layout (std140) uniform DisplayObjectUBO {
  vec2 u_translation;
  vec2 u_scale;
  float u_rotation;
};
uniform mat4 u_projection;

out vec2 v_texcoord;

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

void main(void) {

  vec2 moveToTopLeft = u_scale / 2.0;

  mat4 transform = translate2d(moveToTopLeft + u_translation) * scale2d(u_scale);

  gl_Position = u_projection * transform * a_vertices;

  v_texcoord = a_texcoord;
}
