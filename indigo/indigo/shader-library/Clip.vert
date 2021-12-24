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
  float CLIP_SHEET_FRAME_COUNT;
  float CLIP_SHEET_FRAME_DURATION;
  float CLIP_SHEET_WRAP_AT;
  float CLIP_SHEET_ARRANGEMENT; // 0 = horizontal, 1 = vertical
  float CLIP_SHEET_START_OFFSET;
  float CLIP_PLAY_DIRECTION; // 0 = forward, 1 = backward, 2 = ping pong
  float CLIP_PLAYMODE_START_TIME;
  float CLIP_PLAYMODE_TIMES;
};

void vertex(){

  float clipTotalTime;
  float currentFrame;

  int direction = int(round(CLIP_PLAY_DIRECTION));

   // 0 = forward, 1 = backward, 2 = ping pong
  switch(direction) {
    case 0:
      clipTotalTime = CLIP_SHEET_FRAME_COUNT * CLIP_SHEET_FRAME_DURATION;
      currentFrame = floor(mod(TIME, clipTotalTime) / CLIP_SHEET_FRAME_DURATION);
      break;

    case 1:
      clipTotalTime = CLIP_SHEET_FRAME_COUNT * CLIP_SHEET_FRAME_DURATION;
      currentFrame = floor(mod(TIME, clipTotalTime) / CLIP_SHEET_FRAME_DURATION);
      currentFrame = CLIP_SHEET_FRAME_COUNT - 1.0 - currentFrame;
      break;

    case 2:
      clipTotalTime = CLIP_SHEET_FRAME_COUNT * 2.0 * CLIP_SHEET_FRAME_DURATION;
      currentFrame = floor(mod(TIME, clipTotalTime) / CLIP_SHEET_FRAME_DURATION);

      if(currentFrame >= CLIP_SHEET_FRAME_COUNT) {
        currentFrame = (CLIP_SHEET_FRAME_COUNT * 2.0) - 1.0 - currentFrame;
      }

      break;

    default:
      clipTotalTime = 0.0;
      currentFrame = 0.0;
      break;
  }

  float x;
  float y;
  int arrangement = int(round(CLIP_SHEET_ARRANGEMENT));

  // 0 = horizontal, 1 = vertical
  switch(arrangement) {
    case 0:
      x = mod(currentFrame, CLIP_SHEET_WRAP_AT);
      y = floor(currentFrame / CLIP_SHEET_WRAP_AT);
      break;

    case 1:
      x = floor(currentFrame / CLIP_SHEET_WRAP_AT);
      y = mod(currentFrame, CLIP_SHEET_WRAP_AT);
      break;

    default:
      x = 0.0;
      y = 0.0;
      break;
  }

  CHANNEL_0_TEXTURE_COORDS = scaleCoordsWithOffset(UV + vec2(x, y), CHANNEL_0_ATLAS_OFFSET);
}
//</indigo-vertex>
