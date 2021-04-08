#version 300 es

precision lowp float;

in vec2 SIZE; // In this case, screen size.
in vec2 UV; // Unscaled texture coordinates

uniform sampler2D SRC_CHANNEL;
uniform sampler2D DST_CHANNEL;

out vec4 fragColor;

// public
layout (std140) uniform IndigoFrameData {
  float TIME; // Running time
  vec2 VIEWPORT_SIZE; // Size of the viewport in pixels
};

// Constants
const float PI = 3.141592653589793;
const float PI_2 = PI * 0.5;
const float PI_4 = PI * 0.25;
const float TAU = 2.0 * PI;
const float TAU_2 = PI;
const float TAU_4 = PI_2;
const float TAU_8 = PI_4;

vec4 SRC; // Pixel value from SRC texture
vec4 DST; // Pixel value from DST texture

// Output
vec4 COLOR;

//#fragment_start
void fragment(){}
//#fragment_end

//#light_start
void light(){} // Placeholder only to appease src generator. No lights used.
//#light_end

void main(void) {

  SRC = texture(SRC_CHANNEL, UV);
  DST = texture(DST_CHANNEL, UV);
  COLOR = vec4(0.0);

  // Colour
  fragment();

  fragColor = COLOR;
}
