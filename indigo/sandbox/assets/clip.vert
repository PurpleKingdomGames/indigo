#version 300 es

precision mediump float;

vec2 CHANNEL_0_TEXTURE_COORDS;
vec2 CHANNEL_0_ATLAS_OFFSET;
vec2 UV;

float TIME;

vec2 scaleCoordsWithOffset(vec2 a, vec2 b){
  return vec2(0.0);
}

//<indigo-vertex>
void vertex(){
  int frameCount = 3;
  int wrapAt = 2;
  float frameDuration = 0.5;
  float totalTime = float(frameCount) * frameDuration;
  
  float currentFrame = floor(mod(TIME, totalTime) / frameDuration);
  float x = mod(currentFrame, float(wrapAt));
  float y = floor(currentFrame / float(wrapAt));

  CHANNEL_0_TEXTURE_COORDS = scaleCoordsWithOffset(UV + vec2(x, y), CHANNEL_0_ATLAS_OFFSET);
}
//</indigo-vertex>
