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
layout (std140) uniform IndigoClipData {
  highp float CLIP_SHEET_FRAME_COUNT;
  highp float CLIP_SHEET_FRAME_DURATION;
  highp float CLIP_SHEET_WRAP_AT;
  highp float CLIP_SHEET_ARRANGEMENT; // 0 = horizontal, 1 = vertical
  highp float CLIP_SHEET_START_OFFSET;
  highp float CLIP_PLAY_DIRECTION; // 0 = forward, 1 = backward, 2 = ping pong
  highp float CLIP_PLAYMODE_START_TIME;
  highp float CLIP_PLAYMODE_TIMES;
};

void vertex(){
  float clipTotalTime = CLIP_SHEET_FRAME_COUNT * CLIP_SHEET_FRAME_DURATION;
  float currentFrame = floor(mod(TIME, clipTotalTime) / CLIP_SHEET_FRAME_DURATION);
  float x = mod(currentFrame, CLIP_SHEET_WRAP_AT);
  float y = floor(currentFrame / CLIP_SHEET_WRAP_AT);

  CHANNEL_0_TEXTURE_COORDS = scaleCoordsWithOffset(UV + vec2(x, y), CHANNEL_0_ATLAS_OFFSET);
}
//</indigo-vertex>
