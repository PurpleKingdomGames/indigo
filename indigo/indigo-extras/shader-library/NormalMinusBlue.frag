#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;

vec4 COLOR;
vec2 UV;
vec2 TEXTURE_SIZE;
vec2 SIZE;

vec4 CHANNEL_0;
vec2 CHANNEL_0_POSITION;
vec2 CHANNEL_0_SIZE;
vec4 CHANNEL_1;
vec2 CHANNEL_1_POSITION;
vec4 CHANNEL_2;
vec2 CHANNEL_2_POSITION;
vec4 CHANNEL_3;
vec2 CHANNEL_3_POSITION;

//<indigo-fragment>
layout (std140) uniform IndigoBitmapData {
  highp float FILLTYPE;
};

vec2 stretchedUVs(vec2 pos, vec2 size) {
  return pos + UV * size;
}

vec2 tiledUVs(vec2 pos, vec2 size) {
  return pos + (fract(UV * (SIZE / TEXTURE_SIZE)) * size);
}

void fragment(){

  // 0 = normal; 1 = stretch; 2 = tile
  int fillType = int(round(FILLTYPE));

  switch(fillType) {
    case 1:
      CHANNEL_0 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_0_POSITION, CHANNEL_0_SIZE));
      CHANNEL_1 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_1_POSITION, CHANNEL_0_SIZE));
      CHANNEL_2 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_2_POSITION, CHANNEL_0_SIZE));
      CHANNEL_3 = texture(SRC_CHANNEL, stretchedUVs(CHANNEL_3_POSITION, CHANNEL_0_SIZE));
      break;

    case 2:
      CHANNEL_0 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_0_POSITION, CHANNEL_0_SIZE));
      CHANNEL_1 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_1_POSITION, CHANNEL_0_SIZE));
      CHANNEL_2 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_2_POSITION, CHANNEL_0_SIZE));
      CHANNEL_3 = texture(SRC_CHANNEL, tiledUVs(CHANNEL_3_POSITION, CHANNEL_0_SIZE));
      break;

    default:
      break;
  }

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
