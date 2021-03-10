#version 300 es

precision lowp float;

layout (location = 0) in vec4 a_verticesAndCoords; // a_vertices, a_texcoord

layout (std140) uniform IndigoMergeData {
  mat4 u_projection;
  vec2 u_translation;
  vec2 u_scale;
  vec2 u_frameTranslation;
  vec2 u_frameScale;
};

layout (std140) uniform IndigoFrameData {
  float TIME; // Running time
  vec2 VIEWPORT_SIZE; // Size of the viewport in pixels
};

out vec2 TEXTURE_COORDS;
out vec2 SIZE;
out vec2 UV;

// Constants
const float TAU = 2.0 * 3.141592653589793;
const float PI = 3.141592653589793;

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

vec4 VERTEX;

//#vertex_start
void vertex(){}
//#vertex_end

void main(void) {

  TEXTURE_COORDS = scaleTextCoords();
  SIZE = u_scale;
  VERTEX = vec4(a_verticesAndCoords.x, a_verticesAndCoords.y, 1.0, 1.0);
  UV = a_verticesAndCoords.zw;

  vertex();

  vec2 moveToTopLeft = u_scale / 2.0;

  mat4 transform = translate2d(moveToTopLeft + u_translation) * scale2d(u_scale);

  gl_Position = u_projection * transform * VERTEX;

}
