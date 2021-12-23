#version 300 es

precision mediump float;

vec2 CHANNEL_0_TEXTURE_COORDS;
vec2 CHANNEL_0_ATLAS_OFFSET;
vec2 UV;

float TIME;

vec2 scaleCoordsWithOffset(vec2 a, vec2 b){
  return vec2(0.0);
}

/*
Horizontal / Vertical

ClipSheet(
    frameCount: Int,
    frameDuration: Seconds,
    wrapAt: Int,
    arrangement: ClipSheetArrangement,
    startOffset: Int)

case Forward, Backward, PingPong

enum ClipPlayMode:
  val direction: ClipPlayDirection

  case Loop(direction: ClipPlayDirection) extends ClipPlayMode
  case PlayOnce(direction: ClipPlayDirection, startTime: Seconds) extends ClipPlayMode
  case PlayCount(direction: ClipPlayDirection, startTime: Seconds, times: Int) extends ClipPlayMode
*/

//<indigo-vertex>
layout (std140) uniform IndigoClipData {
  float CLIP_FRAME_COUNT;
  float CLIP_FRAME_DURATION;
  float CLIP_WRAP_AT;
};

void vertex(){
  float clipTotalTime = CLIP_FRAME_COUNT * CLIP_FRAME_DURATION;
  float currentFrame = floor(mod(TIME, clipTotalTime) / CLIP_FRAME_DURATION);
  float x = mod(currentFrame, CLIP_WRAP_AT);
  float y = floor(currentFrame / CLIP_WRAP_AT);

  CHANNEL_0_TEXTURE_COORDS = scaleCoordsWithOffset(UV + vec2(x, y), CHANNEL_0_ATLAS_OFFSET);
}
//</indigo-vertex>
