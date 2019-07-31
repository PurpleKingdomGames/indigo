#version 300 es

layout (location = 0) in vec4 a_vertices;
layout (location = 1) in vec2 a_texcoord;

layout (std140) uniform DisplayObjectUBO {
  mat4 u_projection;
  vec2 u_translation;
  vec2 u_scale;
  vec2 u_frameTranslation;
  vec2 u_frameScale;
  vec4 u_gameOverlay;
  vec4 u_uiOverlay;
  vec4 u_gameLayerTint;
  vec4 u_lightingLayerTint;
  vec4 u_uiLayerTint;
  float u_gameLayerSaturation;
  float u_lightingLayerSaturation;
  float u_uiLayerSaturation;
};

out vec2 v_texcoord;
out vec4 v_gameOverlay;
out vec4 v_uiOverlay;

out vec4 v_gameLayerTint;
out vec4 v_lightingLayerTint;
out vec4 v_uiLayerTint;

out float v_gameLayerSaturation;
out float v_lightingLayerSaturation;
out float v_uiLayerSaturation;

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
  return (transform * vec4(a_texcoord.x, a_texcoord.y, 1, 1)).xy;
}

void main(void) {

  vec2 moveToTopLeft = u_scale / 2.0;

  mat4 transform = translate2d(moveToTopLeft + u_translation) * scale2d(u_scale);

  gl_Position = u_projection * transform * a_vertices;

  v_texcoord = scaleTextCoords();

  v_gameOverlay = u_gameOverlay;
  v_uiOverlay = u_uiOverlay;

  v_gameLayerTint = u_gameLayerTint;
  v_lightingLayerTint = u_lightingLayerTint;
  v_uiLayerTint = u_uiLayerTint;

  v_gameLayerSaturation = u_gameLayerSaturation;
  v_lightingLayerSaturation = u_lightingLayerSaturation;
  v_uiLayerSaturation = u_uiLayerSaturation;
}
