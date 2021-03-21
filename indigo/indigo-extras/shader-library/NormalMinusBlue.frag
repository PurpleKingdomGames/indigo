#version 300 es

precision mediump float;

vec4 CHANNEL_0;
vec4 COLOR;

//<indigo-fragment>
void fragment(){
  vec3 redGreen = vec3(CHANNEL_0.rg, 0.0);
  float alpha;
  
  if(abs(redGreen.r - 0.5) < 0.01 && abs(redGreen.g - 0.5) < 0.01) {
    alpha = 0.0;
  } else {
    alpha = max(redGreen.r, redGreen.g);
  }

  COLOR = vec4(CHANNEL_0.rg * alpha, 0.0, alpha);
}
//</indigo-fragment>
