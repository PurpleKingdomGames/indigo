attribute vec4 coordinates;
attribute vec2 a_texcoord;
attribute vec4 a_effectValues;

uniform mat4 u_matrix;

varying vec2 v_texcoord;
varying vec4 v_effectValues;

void main(void) {
  gl_Position = u_matrix * coordinates;

  // Pass the texcoord to the fragment shader.
  v_texcoord = a_texcoord;
  v_effectValues = a_effectValues;
}
