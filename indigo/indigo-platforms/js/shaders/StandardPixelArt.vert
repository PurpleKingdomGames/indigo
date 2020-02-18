#version 300 es

layout (location = 0) in vec4 a_vertices;
layout (location = 1) in vec2 a_texcoord;
layout (location = 2) in vec2 a_translation;
layout (location = 3) in vec2 a_scale;
layout (location = 4) in vec4 a_tint;
layout (location = 5) in vec2 a_frameTranslation;
layout (location = 6) in vec2 a_frameScale;
layout (location = 7) in float a_rotation;
layout (location = 8) in float a_fliph;
layout (location = 9) in float a_flipv;
layout (location = 10) in float a_alpha;
layout (location = 11) in vec2 a_ref;
layout (location = 12) in vec2 a_size;

uniform mat4 u_projection;

out vec2 v_texcoord;
out vec4 v_tint;
out float v_alpha;
out vec2 v_size;
out vec2 v_textureOffsets3x3[9];

mat4 rotate2d(float angle){
    return mat4(cos(angle), -sin(angle), 0, 0,
                sin(angle), cos(angle), 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
                );
}

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

vec2 scaleTextCoords(vec2 texcoord){
  mat4 transform = translate2d(a_frameTranslation) * scale2d(a_frameScale);
  return (transform * vec4(texcoord, 1.0, 1.0)).xy;
}

vec2 sizeOfAPixel() {
  return (scale2d(a_frameScale) * vec4(1.0)).xy;
}

void main(void) {

  vec2 moveToReferencePoint = -(a_ref / a_size) + 0.5;

  mat4 transform = 
    translate2d(a_translation) * 
    rotate2d(a_rotation) * 
    scale2d(a_size * a_scale) * 
    translate2d(moveToReferencePoint) * 
    scale2d(vec2(a_fliph, a_flipv));

  gl_Position = u_projection * transform * a_vertices;

  v_texcoord = scaleTextCoords(a_texcoord);
  v_tint = a_tint;
  v_alpha = a_alpha;
  v_size = a_size;

  vec2 offsets3x3[9] = vec2[9](
    vec2(-1, -1),
    vec2( 0, -1),
    vec2( 1, -1),
    vec2(-1,  0),
    vec2( 0,  0),
    vec2( 1,  0),
    vec2(-1,  1),
    vec2( 0,  1),
    vec2( 1,  1)
  );

  vec2 onePixel = sizeOfAPixel();

  v_textureOffsets3x3 = vec2[9](
    scaleTextCoords(a_texcoord + (onePixel * offsets3x3[0])),
    scaleTextCoords(a_texcoord + (onePixel * offsets3x3[1])),
    scaleTextCoords(a_texcoord + (onePixel * offsets3x3[2])),
    scaleTextCoords(a_texcoord + (onePixel * offsets3x3[3])),
    scaleTextCoords(a_texcoord + (onePixel * offsets3x3[4])),
    scaleTextCoords(a_texcoord + (onePixel * offsets3x3[5])),
    scaleTextCoords(a_texcoord + (onePixel * offsets3x3[6])),
    scaleTextCoords(a_texcoord + (onePixel * offsets3x3[7])),
    scaleTextCoords(a_texcoord + (onePixel * offsets3x3[8]))
  );

}
