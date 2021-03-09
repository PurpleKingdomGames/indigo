#version 300 es

precision lowp float;

in vec2 TEXTURE_COORDS; // texture coordinates on atlas
in vec2 SIZE; // In this case, screen size.
in vec2 UV; // Unscaled texture coordinates

uniform sampler2D u_channel_0;

out vec4 fragColor;

// public
layout (std140) uniform IndigoFrameData {
  float TIME; // Running time
};

// Constants
const float TAU = 2.0 * 3.141592653589793;
const float PI = 3.141592653589793;

vec4 SRC; // Pixel value from SRC texture

// Output
vec4 COLOR;

//#fragment_start
void fragment(){}
//#fragment_end

//#light_start
void light(){} // Placeholder only to appeal src generator. No lights used.
//#light_end

void main(void) {

  SRC = texture(u_channel_0, TEXTURE_COORDS);
  COLOR = SRC;

  // Colour
  fragment();

  fragColor = COLOR;
}
