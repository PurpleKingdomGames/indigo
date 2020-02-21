#version 300 es

layout (location = 0) in vec4 a_vertices;
layout (location = 1) in vec2 a_texcoord;
layout (location = 2) in vec2 a_translation;
layout (location = 3) in vec2 a_scale;
layout (location = 4) in vec2 a_frameTranslation;
layout (location = 5) in vec2 a_frameScale;
layout (location = 6) in float a_rotation;
layout (location = 7) in vec2 a_ref;
layout (location = 8) in vec2 a_size;
layout (location = 9) in vec4 a_tint;
layout (location = 10) in vec4 a_colorOverlay;
layout (location = 11) in vec4 a_gradiantOverlayFrom;
layout (location = 12) in vec4 a_gradiantOverlayTo;
layout (location = 13) in vec4 a_gradiantOverlayFromColor;
layout (location = 14) in vec4 a_gradiantOverlayToColor;
layout (location = 15) in vec4 a_outerBorderColor;
layout (location = 16) in vec4 a_outerBorderAmount;
layout (location = 17) in vec4 a_innerBorderColor;
layout (location = 18) in vec4 a_innerBorderAmount;
layout (location = 19) in vec4 a_outerGlowColor;
layout (location = 20) in vec4 a_outerGlowAmount;
layout (location = 21) in vec4 a_innerGlowColor;
layout (location = 22) in vec4 a_innerGlowAmount;
layout (location = 23) in float a_blur;
layout (location = 24) in float a_alpha;
layout (location = 25) in float a_fliph;
layout (location = 26) in float a_flipv;

uniform mat4 u_projection;

out vec2 v_texcoord;
out vec4 v_tint;
out float v_alpha;

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

vec2 scaleTextCoords(){
  mat4 transform = translate2d(a_frameTranslation) * scale2d(a_frameScale);
  return (transform * vec4(a_texcoord.x, a_texcoord.y, 1, 1)).xy;
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

  // Pass the texcoord to the fragment shader.
  v_texcoord = scaleTextCoords();
  v_tint = a_tint;
  v_alpha = a_alpha;
}
