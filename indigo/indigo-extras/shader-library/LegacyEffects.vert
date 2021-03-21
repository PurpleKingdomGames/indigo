#version 300 es

precision lowp float;

// These values and functions are all placeholders and will be replaced with the real ones
// at compile time.
vec2 TEXTURE_COORDS;
vec2 SIZE;
vec2 CHANNEL_0_ATLAS_OFFSET;

mat4 scale2d(vec2 s){
    return mat4(s.x, 0, 0, 0,
                0, s.y, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
                );
}

vec2 scaleTexCoordsWithOffset(vec2 texcoord, vec2 offset){
  return vec2(0.0); // placeholder
}

//<indigo-vertex>
out vec2 v_offsetTL;
out vec2 v_offsetTC;
out vec2 v_offsetTR;
out vec2 v_offsetML;
out vec2 v_offsetMC;
out vec2 v_offsetMR;
out vec2 v_offsetBL;
out vec2 v_offsetBC;
out vec2 v_offsetBR;

vec2 scaleTexCoords(vec2 texcoord){
  return scaleTexCoordsWithOffset(TEXTURE_COORDS, CHANNEL_0_ATLAS_OFFSET);
}

vec2 sizeOfAPixel() {
  return (scale2d(1.0 / SIZE) * vec4(1.0)).xy;
}

const vec2[9] gridOffsets = vec2[9](
  vec2(-1.0, -1.0),
  vec2(0.0, -1.0),
  vec2(1.0, -1.0),

  vec2(-1.0, 0.0),
  vec2(0.0, 0.0),
  vec2(1.0, 0.0),

  vec2(-1.0, 1.0),
  vec2(0.0, 1.0),
  vec2(1.0, 1.0)
);

vec2[9] generateTexCoords3x3(vec2 texcoords, vec2 onePixel) {
  return vec2[9](
    scaleTexCoords(texcoords + (onePixel * gridOffsets[0])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[1])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[2])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[3])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[4])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[5])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[6])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[7])),
    scaleTexCoords(texcoords + (onePixel * gridOffsets[8]))
  );
}

void vertex(){
  vec2 offsets[9] = generateTexCoords3x3(TEXTURE_COORDS, sizeOfAPixel());

  v_offsetTL = offsets[0];
  v_offsetTC = offsets[1];
  v_offsetTR = offsets[2];
  v_offsetML = offsets[3];
  v_offsetMC = offsets[4];
  v_offsetMR = offsets[5];
  v_offsetBL = offsets[6];
  v_offsetBC = offsets[7];
  v_offsetBR = offsets[8];
}
//</indigo-vertex>
