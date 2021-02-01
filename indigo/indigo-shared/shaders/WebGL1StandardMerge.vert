// Attributes
attribute vec4 a_verticesAndCoords;

// Uniforms
uniform mat4 u_projection;
uniform mat4 u_transform;
uniform float u_alpha;
uniform vec4 u_frameTransform; // fine

varying vec2 v_texcoord;
varying float v_alpha;

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
  mat4 transform = translate2d(offset) * scale2d(u_frameTransform.zw);
  return (transform * vec4(texcoord, 1.0, 1.0)).xy;
}

vec2 scaleTexCoords(vec2 texcoord){
  return scaleTexCoordsWithOffset(texcoord, u_frameTransform.xy);
}

void main(void) {
  vec4 vertices = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
  vec2 texcoords = a_verticesAndCoords.zw;

  gl_Position = u_projection * u_transform * vertices;

  v_texcoord = scaleTexCoords(texcoords);
  v_alpha = u_alpha;
}
