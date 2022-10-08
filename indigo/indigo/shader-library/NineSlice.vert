#version 300 es

precision mediump float;

vec2 SIZE;
vec2 UV;

//<indigo-vertex>
layout (std140) uniform IndigoNineSliceData {
  highp float SOME_VALUE;
};

//Actual Size(128, 64)
float SLICE_X1 = 10.0;
float SLICE_X2 = 50.0;
float SLICE_Y1 = 20.0;
float SLICE_Y2 = 40.0;

vec2 mod2(vec2 x, vec2 y) {
  return x - y * floor(x / y);
}

void vertex(){

  // vec2 foo = fract(UV * 2.0);

  // UV = foo; 

  // Convert UV to pixels
  SIZE = vec2(128, 64);
  float pixels = SIZE.x * UV.x; // at 0.5x0.5, 64x32 

  float px = float(int(pixels) % 10);
  // float py = float(int(pixels.y) % 10);

  // vec2 wrapped = vec2(px, py); // 4x2
  
  UV = vec2(px * 0.1, 0.0); // 0.4x0.2
  // UV = pixels / SIZE;
  // UV = vec2(mod(UV.x * 100.0, 100.0), mod(UV.y * 100.0, 100.0));// / 100.0;
  // UV = mod2(UV * 99.0, vec2(100.0)) / 100.0;

  // vec2 foo = mod(UV * 100.0, vec2(100.01));

  // UV = foo;//mod((UV * 100.0) * 2.0, vec2(100.0));// * 0.001;
}
//</indigo-vertex>
