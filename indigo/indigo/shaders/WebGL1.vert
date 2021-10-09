// Attributes
attribute vec4 a_verticesAndCoords;

// Uniforms
uniform mat4 u_projection;
uniform vec4 u_translateScale;
uniform vec4 u_refRotation;
uniform vec4 u_frameTransform;
uniform vec4 u_sizeFlip;

// Varying
varying vec2 v_texcoord;

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

mat4 rotate2d(float angle){
    return mat4(cos(angle), -sin(angle), 0, 0,
                sin(angle), cos(angle), 0, 0,
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

  vec2 ref = u_refRotation.xy;
  vec2 size = u_sizeFlip.xy;
  vec2 flip = u_sizeFlip.zw;
  vec2 translation = u_translateScale.xy;
  vec2 scale = u_translateScale.zw;
  float rotation = u_refRotation.w;
  vec2 moveToReferencePoint = -(ref / size) + 0.5;

  mat4 transform = 
    translate2d(translation) *
    rotate2d(-1.0 * rotation) *
    scale2d(size * scale) *
    translate2d(moveToReferencePoint) *
    scale2d(vec2(1.0, -1.0) * flip);

  gl_Position = u_projection * transform * vertices;

  v_texcoord = scaleTexCoords(texcoords);
}
