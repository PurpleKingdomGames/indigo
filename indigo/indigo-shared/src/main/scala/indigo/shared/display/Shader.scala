package indigo.shared.display

trait Shader {
  val id: ShaderId
  val vertex: String
  val fragment: String
}

object Shader {

  // WebGL 2.0 only at the moment.
  def fromCustomShader(customShader: CustomShader.Source): Shader =
    new Shader {
      val id: ShaderId     = customShader.id
      val vertex: String   = ShaderTemplates.webGL2Vertex(customShader.vertex)
      val fragment: String = ShaderTemplates.webGL2Fragment(customShader.fragment)
    }

  object ShaderTemplates {

    def webGL2Vertex(func: String): String =
      s"""#version 300 es
      |
      |precision lowp float;
      |
      |layout (location = 0) in vec4 a_verticesAndCoords; // a_vertices, a_texcoord
      |layout (location = 1) in vec4 a_matRotateScale; // mat(0,1,4,5)
      |layout (location = 2) in vec4 a_matTranslateAlpha; // mat(12,13,14,alpha)
      |layout (location = 3) in vec2 a_size; // mat(12,13,14,alpha)
      |layout (location = 4) in vec4 a_frameTransform; // a_frameTranslation, a_frameScale
      |layout (location = 5) in vec4 a_tint;
      |layout (location = 6) in vec4 a_gradiantPositions; // a_gradiantOverlayFrom, a_gradiantOverlayTo
      |layout (location = 7) in vec4 a_gradiantOverlayFromColor;
      |layout (location = 8) in vec4 a_gradiantOverlayToColor;
      |layout (location = 9) in vec4 a_borderColor;
      |layout (location = 10) in vec4 a_glowColor;
      |layout (location = 11) in vec4 a_amounts; // a_outerBorderAmount, a_innerBorderAmount, a_outerGlowAmount, a_innerGlowAmount
      |layout (location = 12) in vec4 a_emissiveNormalOffsets; // a_emissive (vec2), a_normal (vec2)
      |layout (location = 13) in vec4 a_specularOffsetIsLit; // a_specular (vec2), a_isLit (float), ???
      |layout (location = 14) in vec4 a_textureAmounts; // albedoAmount (float), emissiveAmount (float), normalAmount (float), specularAmount (float)
      |
      |uniform mat4 u_projection;
      |
      |// The varying values are organised this way to help the packer
      |// squash them into 15 varying vectors
      |out vec4 v_texcoordEmissiveNormal;
      |out vec4 v_relativeScreenCoordsIsLitAlpha;
      |out vec4 v_tint;
      |out vec4 v_gradiantFromTo;
      |out vec4 v_gradiantOverlayFromColor;
      |out vec4 v_gradiantOverlayToColor;
      |out vec4 v_borderColor;
      |out vec4 v_glowColor;
      |out vec4 v_effectAmounts;
      |out vec4 v_textureAmounts;
      |out vec2 v_offsetTL;
      |out vec2 v_offsetTC;
      |out vec2 v_offsetTR;
      |out vec2 v_offsetML;
      |out vec2 v_offsetMC;
      |out vec2 v_offsetMR;
      |out vec2 v_offsetBL;
      |out vec2 v_offsetBC;
      |out vec2 v_offsetBR;
      |out vec2 v_texcoordSpecular;
      |out vec2 v_texcoords;
      |
      |mat4 translate2d(vec2 t){
      |    return mat4(1, 0, 0, 0,
      |                0, 1, 0, 0,
      |                0, 0, 1, 0,
      |                t.x, t.y, 0, 1
      |                );
      |}
      |
      |mat4 scale2d(vec2 s){
      |    return mat4(s.x, 0, 0, 0,
      |                0, s.y, 0, 0,
      |                0, 0, 1, 0,
      |                0, 0, 0, 1
      |                );
      |}
      |
      |vec2 scaleTexCoordsWithOffset(vec2 texcoord, vec2 offset){
      |  mat4 transform = translate2d(offset) * scale2d(a_frameTransform.zw);
      |  return (transform * vec4(texcoord, 1.0, 1.0)).xy;
      |}
      |
      |vec2 scaleTexCoords(vec2 texcoord){
      |  return scaleTexCoordsWithOffset(texcoord, a_frameTransform.xy);
      |}
      |
      |vec2 sizeOfAPixel() {
      |  return (scale2d(1.0 / a_size) * vec4(1.0)).xy;
      |}
      |
      |const vec2[9] gridOffsets = vec2[9](
      |  vec2(-1.0, -1.0),
      |  vec2(0.0, -1.0),
      |  vec2(1.0, -1.0),
      |
      |  vec2(-1.0, 0.0),
      |  vec2(0.0, 0.0),
      |  vec2(1.0, 0.0),
      |
      |  vec2(-1.0, 1.0),
      |  vec2(0.0, 1.0),
      |  vec2(1.0, 1.0)
      |);
      |
      |vec2[9] generateTexCoords3x3(vec2 texcoords, vec2 onePixel) {
      |  return vec2[9](
      |    scaleTexCoords(texcoords + (onePixel * gridOffsets[0])),
      |    scaleTexCoords(texcoords + (onePixel * gridOffsets[1])),
      |    scaleTexCoords(texcoords + (onePixel * gridOffsets[2])),
      |    scaleTexCoords(texcoords + (onePixel * gridOffsets[3])),
      |    scaleTexCoords(texcoords + (onePixel * gridOffsets[4])),
      |    scaleTexCoords(texcoords + (onePixel * gridOffsets[5])),
      |    scaleTexCoords(texcoords + (onePixel * gridOffsets[6])),
      |    scaleTexCoords(texcoords + (onePixel * gridOffsets[7])),
      |    scaleTexCoords(texcoords + (onePixel * gridOffsets[8]))
      |  );
      |}
      |
      |vec4 VERTEX;
      |
      |$func
      |
      |void main(void) {
      |
      |  vec4 vertices = vec4(a_verticesAndCoords.xy, 1.0, 1.0);
      |  vec2 texcoords = a_verticesAndCoords.zw;
      |  float alpha = a_matTranslateAlpha.w;
      |  vec2 texcoordsEmissive = a_emissiveNormalOffsets.xy;
      |  vec2 texcoordsNormal = a_emissiveNormalOffsets.zw;
      |  vec2 texcoordsSpecular = a_specularOffsetIsLit.xy;
      |  float isLit = a_specularOffsetIsLit.z;
      |
      |  vec2 offsets[9] = generateTexCoords3x3(texcoords, sizeOfAPixel());
      |
      |  mat4 transform =
      |    mat4(a_matRotateScale.x,    a_matRotateScale.y,    0,                     0,
      |         a_matRotateScale.z,    a_matRotateScale.w,    0,                     0,
      |         0,                     0,                     1,                     0,
      |         a_matTranslateAlpha.x, a_matTranslateAlpha.y, a_matTranslateAlpha.z, 1
      |        );
      |
      |  VERTEX = u_projection * transform * vertices;
      |  vertex();
      |  gl_Position = VERTEX;
      |
      |  v_texcoordEmissiveNormal = vec4(scaleTexCoordsWithOffset(texcoords, texcoordsEmissive), scaleTexCoordsWithOffset(texcoords, texcoordsNormal));
      |  v_relativeScreenCoordsIsLitAlpha = vec4(texcoords * a_size, isLit, alpha);
      |  v_tint = a_tint;
      |  v_gradiantFromTo = a_gradiantPositions;
      |  v_gradiantOverlayFromColor = a_gradiantOverlayFromColor;
      |  v_gradiantOverlayToColor = a_gradiantOverlayToColor;
      |  v_borderColor = a_borderColor;
      |  v_glowColor = a_glowColor;
      |  v_effectAmounts = a_amounts;
      |  v_textureAmounts = a_textureAmounts;
      |  v_offsetTL = offsets[0];
      |  v_offsetTC = offsets[1];
      |  v_offsetTR = offsets[2];
      |  v_offsetML = offsets[3];
      |  v_offsetMC = offsets[4];
      |  v_offsetMR = offsets[5];
      |  v_offsetBL = offsets[6];
      |  v_offsetBC = offsets[7];
      |  v_offsetBR = offsets[8];
      |  v_texcoordSpecular = scaleTexCoordsWithOffset(texcoords, texcoordsSpecular);
      |  v_texcoords = a_verticesAndCoords.zw;
      |
      |
      |}
      |
      |""".stripMargin

    def webGL2Fragment(func: String): String =
      s"""#version 300 es
      |
      |precision lowp float;
      |
      |uniform sampler2D u_texture;
      |
      |in vec4 v_texcoordEmissiveNormal;
      |in vec4 v_relativeScreenCoordsIsLitAlpha;
      |in vec4 v_tint;
      |in vec4 v_gradiantFromTo;
      |in vec4 v_gradiantOverlayFromColor;
      |in vec4 v_gradiantOverlayToColor;
      |in vec4 v_borderColor;
      |in vec4 v_glowColor;
      |in vec4 v_effectAmounts;
      |in vec4 v_textureAmounts;
      |in vec2 v_offsetTL;
      |in vec2 v_offsetTC;
      |in vec2 v_offsetTR;
      |in vec2 v_offsetML;
      |in vec2 v_offsetMC;
      |in vec2 v_offsetMR;
      |in vec2 v_offsetBL;
      |in vec2 v_offsetBC;
      |in vec2 v_offsetBR;
      |in vec2 v_texcoordSpecular;
      |in vec2 v_texcoords;
      |
      |layout(location = 0) out vec4 albedo;
      |layout(location = 1) out vec4 emissive;
      |layout(location = 2) out vec4 normal;
      |layout(location = 3) out vec4 specular;
      |
      |vec4 applyBasicEffects(vec4 textureColor) {
      |  vec4 withAlpha = vec4(textureColor.rgb, textureColor.a * v_relativeScreenCoordsIsLitAlpha.w);
      |
      |  vec4 tintedVersion = vec4(withAlpha.rgb * v_tint.rgb, withAlpha.a);
      |
      |  return mix(withAlpha, tintedVersion, max(0.0, v_tint.a));
      |}
      |
      |vec4 calculateGradiantOverlay() {
      |  vec2 pointA = v_gradiantFromTo.xy;
      |  vec2 pointB = v_gradiantFromTo.zw;
      |  vec2 pointP = v_relativeScreenCoordsIsLitAlpha.xy;
      |
      |  // `h` is the distance along the gradiant 0 at A, 1 at B
      |  float h = min(1.0, max(0.0, dot(pointP - pointA, pointB - pointA) / dot(pointB - pointA, pointB - pointA)));
      |
      |  return mix(v_gradiantOverlayFromColor, v_gradiantOverlayToColor, h);
      |}
      |
      |const float border1px[9] = float[9](
      |  0.0, 1.0, 0.0,
      |  1.0, 0.0, 1.0,
      |  0.0, 1.0, 0.0
      |);
      |
      |const float border2px[9] = float[9](
      |  1.0, 1.0, 1.0,
      |  1.0, 0.0, 1.0,
      |  1.0, 1.0, 1.0
      |);
      |
      |vec4 calculateOuterBorder(float baseAlpha, float[9] alphas, float amount) {
      |  vec4 outColor = vec4(0.0);
      |  float checkedAmount = clamp(amount, 0.0, 2.0);
      |  float[9] kernel;
      |
      |  if(baseAlpha > 0.001) {
      |    return outColor;
      |  }
      |  
      |  if(abs(checkedAmount) >= 0.0 && abs(checkedAmount) < 0.001) {
      |    return outColor;
      |  }
      |  if(abs(checkedAmount) >= 0.999 && abs(checkedAmount) < 1.001) {
      |    kernel = border1px;
      |  }
      |  if(abs(checkedAmount) >= 1.999 && abs(checkedAmount) < 2.001) {
      |    kernel = border2px;
      |  }
      |
      |  float alphaSum =
      |    alphas[0] * kernel[0] +
      |    alphas[1] * kernel[1] +
      |    alphas[2] * kernel[2] +
      |    alphas[3] * kernel[3] +
      |    alphas[4] * kernel[4] +
      |    alphas[5] * kernel[5] +
      |    alphas[6] * kernel[6] +
      |    alphas[7] * kernel[7] +
      |    alphas[8] * kernel[8];
      |
      |  if(alphaSum > 0.0) {
      |    outColor = v_borderColor;
      |  }
      |
      |  return outColor;
      |}
      |
      |vec4 calculateInnerBorder(float baseAlpha, float[9] alphas, float amount) {
      |  vec4 outColor = vec4(0.0);
      |  float checkedAmount = clamp(amount, 0.0, 2.0);
      |  float[9] kernel;
      |
      |  if(baseAlpha < 0.001) {
      |    return outColor;
      |  }
      |
      |  if(abs(checkedAmount) >= 0.0 && abs(checkedAmount) < 0.001) {
      |    return outColor;
      |  }
      |  if(abs(checkedAmount) >= 0.999 && abs(checkedAmount) < 1.001) {
      |    kernel = border1px;
      |  }
      |  if(abs(checkedAmount) >= 1.999 && abs(checkedAmount) < 2.001) {
      |    kernel = border2px;
      |  }
      |
      |  float alphaSum =
      |    floor(alphas[0]) * kernel[0] +
      |    floor(alphas[1]) * kernel[1] +
      |    floor(alphas[2]) * kernel[2] +
      |    floor(alphas[3]) * kernel[3] +
      |    floor(alphas[4]) * kernel[4] +
      |    floor(alphas[5]) * kernel[5] +
      |    floor(alphas[6]) * kernel[6] +
      |    floor(alphas[7]) * kernel[7] +
      |    floor(alphas[8]) * kernel[8];
      |
      |  if(alphaSum > 0.0) {
      |    outColor = v_borderColor;
      |  }
      |
      |  return outColor;
      |}
      |
      |const float glowKernel[9] = float[9](
      |  1.0, 0.5, 1.0,
      |  0.5, 0.0, 0.5,
      |  1.0, 0.5, 1.0
      |);
      |// glowKernel values summed up.
      |const float glowKernelWeight = 6.0;
      |
      |vec4 calculateOuterGlow(float baseAlpha, float[9] alphas, float amount) {
      |  vec4 outColor = vec4(0.0);
      |
      |  if(baseAlpha > 0.01) {
      |    return outColor;
      |  }
      |
      |  float alphaSum =
      |    alphas[0] * glowKernel[0] +
      |    alphas[1] * glowKernel[1] +
      |    alphas[2] * glowKernel[2] +
      |    alphas[3] * glowKernel[3] +
      |    alphas[4] * glowKernel[4] +
      |    alphas[5] * glowKernel[5] +
      |    alphas[6] * glowKernel[6] +
      |    alphas[7] * glowKernel[7] +
      |    alphas[8] * glowKernel[8];
      |
      |  if(alphaSum > 0.0) {
      |    float checkedAmount = max(0.0, amount);
      |    float glowAmount = (alphaSum / glowKernelWeight) * checkedAmount;
      |    outColor = vec4(v_glowColor.rgb, v_glowColor.a * glowAmount);
      |  }
      |
      |  return outColor;
      |}
      |
      |vec4 calculateInnerGlow(float baseAlpha, float[9] alphas, float amount) {
      |  vec4 outColor = vec4(0.0);
      |
      |  if(baseAlpha < 0.01) {
      |    return outColor;
      |  }
      |
      |  float alphaSum =
      |    floor(alphas[0]) * glowKernel[0] +
      |    floor(alphas[1]) * glowKernel[1] +
      |    floor(alphas[2]) * glowKernel[2] +
      |    floor(alphas[3]) * glowKernel[3] +
      |    floor(alphas[4]) * glowKernel[4] +
      |    floor(alphas[5]) * glowKernel[5] +
      |    floor(alphas[6]) * glowKernel[6] +
      |    floor(alphas[7]) * glowKernel[7] +
      |    floor(alphas[8]) * glowKernel[8];
      |
      |  if(alphaSum > 0.0) {
      |    float checkedAmount = max(0.0, amount);
      |    float glowAmount = (alphaSum / glowKernelWeight) * checkedAmount;
      |    outColor = vec4(v_glowColor.rgb, v_glowColor.a * glowAmount);
      |  }
      |
      |  return outColor;
      |}
      |
      |vec4 calculateNormal(vec4 normalColor, float alpha) {
      |  if (v_relativeScreenCoordsIsLitAlpha.z > 0.0) {
      |    if(normalColor.a < 0.001) {
      |      return vec4(0.5, 0.5, 1.0, alpha);
      |    } else {
      |      return vec4(normalColor.rgb, alpha);
      |    }
      |  } else {
      |    return vec4(0.0, 0.0, 0.0, alpha);
      |  }
      |}
      |
      |vec4 calculateSpecular(vec4 specularColor, float alpha) {
      |  if(specularColor.a < 0.001) {
      |    return vec4(1.0, 1.0, 1.0, alpha);
      |  } else {
      |    return vec4(specularColor.rgb, alpha);
      |  }
      |}
      |
      |vec4 COLOR;
      |vec2 UV;
      |
      |$func
      |
      |void main(void) {
      |
      |  vec2 texcoord = v_offsetMC;
      |  UV = v_texcoords;
      |
      |  float[9] sampledRegionAlphas = float[9](
      |    applyBasicEffects(texture(u_texture, v_offsetTL)).a,
      |    applyBasicEffects(texture(u_texture, v_offsetTC)).a,
      |    applyBasicEffects(texture(u_texture, v_offsetTR)).a,
      |    applyBasicEffects(texture(u_texture, v_offsetML)).a,
      |    applyBasicEffects(texture(u_texture, v_offsetMC)).a,
      |    applyBasicEffects(texture(u_texture, v_offsetMR)).a,
      |    applyBasicEffects(texture(u_texture, v_offsetBL)).a,
      |    applyBasicEffects(texture(u_texture, v_offsetBC)).a,
      |    applyBasicEffects(texture(u_texture, v_offsetBR)).a
      |  );
      |
      |  float[9] sampledRegionAlphasInverse = float[9](
      |    (1.0 - sampledRegionAlphas[0]),
      |    (1.0 - sampledRegionAlphas[1]),
      |    (1.0 - sampledRegionAlphas[2]),
      |    (1.0 - sampledRegionAlphas[3]),
      |    (1.0 - sampledRegionAlphas[4]),
      |    (1.0 - sampledRegionAlphas[5]),
      |    (1.0 - sampledRegionAlphas[6]),
      |    (1.0 - sampledRegionAlphas[7]),
      |    (1.0 - sampledRegionAlphas[8])
      |  );
      |
      |  vec4 baseColor = applyBasicEffects(texture(u_texture, texcoord));
      |
      |  float outerBorderAmount = v_effectAmounts.x;
      |  float innerBorderAmount = v_effectAmounts.y;
      |  float outerGlowAmount = v_effectAmounts.z;
      |  float innerGlowAmount = v_effectAmounts.w;
      |
      |  vec4 overlay = calculateGradiantOverlay();
      |  vec4 innerGlow = calculateInnerGlow(baseColor.a, sampledRegionAlphasInverse, innerGlowAmount);
      |  vec4 outerGlow = calculateOuterGlow(baseColor.a, sampledRegionAlphas, outerGlowAmount);
      |  vec4 innerBorder = calculateInnerBorder(baseColor.a, sampledRegionAlphasInverse, innerBorderAmount);
      |  vec4 outerBorder = calculateOuterBorder(baseColor.a, sampledRegionAlphas, outerBorderAmount);
      |
      |  vec4 withOverlay = vec4(mix(baseColor.rgb, overlay.rgb, overlay.a), baseColor.a);
      |  vec4 withInnerGlow = vec4(mix(withOverlay.rgb, innerGlow.rgb, innerGlow.a), withOverlay.a);
      |  vec4 withOuterGlow = mix(withInnerGlow, outerGlow, outerGlow.a);
      |  vec4 withInnerBorder = mix(withOuterGlow, innerBorder, innerBorder.a);
      |  vec4 withOuterBorder = mix(withInnerBorder, outerBorder, outerBorder.a);
      |
      |  vec4 outColor = withOuterBorder;
      |
      |  float albedoAmount = v_textureAmounts.x;
      |  float emissiveAmount = v_textureAmounts.y;
      |  float normalAmount = v_textureAmounts.z;
      |  float specularAmount = v_textureAmounts.w;
      |
      |  COLOR = vec4(outColor.rgb, outColor.a * albedoAmount);
      |  fragment();
      |  albedo = COLOR;
      |
      |  emissive = texture(u_texture, v_texcoordEmissiveNormal.xy) * emissiveAmount;
      |
      |  normal = vec4(0.0, 0.0, 0.0, outColor.a);
      |  if (v_relativeScreenCoordsIsLitAlpha.z > 0.0) {
      |    normal = mix(
      |      vec4(0.5, 0.5, 1.0, outColor.a),
      |      calculateNormal(texture(u_texture, v_texcoordEmissiveNormal.zw), outColor.a),
      |      normalAmount
      |    );
      |  }
      |
      |  specular = 
      |    mix(
      |      vec4(1.0, 1.0, 1.0, outColor.a),
      |      calculateSpecular(texture(u_texture, v_texcoordSpecular), outColor.a),
      |      specularAmount
      |    );
      |}
      |
      |""".stripMargin

  }

}
