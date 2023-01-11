package indigo.shared.shader.library

import ultraviolet.syntax.*

class LightingTests extends munit.FunSuite {

  // DELETE THESE TESTS THE FIRST TIME THEY BREAK
  // THEY'RE ONLY HERE TO HELP ENSURE THE INITIAL PORT IS OK.

  test("Lighting prepare") {

    val actual =
      Lighting.prepare.output.toOutput.code

    val expected: String =
      """
      |layout (std140) uniform IndigoMaterialLightingData {
      |  highp vec2 LIGHT_EMISSIVE;
      |  highp vec2 LIGHT_NORMAL;
      |  highp vec2 LIGHT_ROUGHNESS;
      |};
      |const float SCREEN_GAMMA=2.200000047683716;
      |vec4 normalColor;
      |vec4 roughnessColor;
      |vec4 emissiveColor;
      |vec4 lightAcc;
      |vec4 specularAcc;
      |mat4 rotationZ(in float angle){
      |  return mat4(cos(angle),-sin(angle),0.0,0.0,sin(angle),cos(angle),0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0);
      |}
      |vec4 calculateLightColor(in float lightAmount){
      |  vec4 color=vec4((LIGHT_COLOR.xyz*lightAmount)*LIGHT_COLOR.w,lightAmount*LIGHT_COLOR.w);
      |  vec4 colorGammaCorrected=pow(color,vec4(1.0/SCREEN_GAMMA));
      |  return colorGammaCorrected;
      |}
      |vec4 calculateLightSpecular(in float lightAmount,in vec3 lightDir,in vec4 normalTexture,in vec4 specularTexture){
      |  float shininess=((specularTexture.x+specularTexture.y)+specularTexture.z)/3.0;
      |  vec3 normal=normalize((2.0*vec3(normalTexture.xy,1.0))-1.0);
      |  vec3 rotatedNormal=(vec4(normal,1.0)*rotationZ(ROTATION)).xyz;
      |  vec3 halfVec=vec3(0.0,0.0,1.0);
      |  float lambertian=max(-dot(rotatedNormal,lightDir),0.0);
      |  vec3 reflection=normalize((vec3(2.0*lambertian))*(rotatedNormal-lightDir));
      |  float specular=(min(pow(dot(reflection,halfVec),shininess),lambertian)*lightAmount)*LIGHT_SPECULAR.w;
      |  return vec4(LIGHT_SPECULAR.xyz*specular,specular);
      |}
      |void prepare(){
      |  lightAcc=vec4(0.0,0.0,0.0,1.0);
      |  specularAcc=vec4(0.0);
      |  emissiveColor=vec4(0.0,0.0,0.0,1.0);
      |  normalColor=vec4(0.5,0.5,1.0,1.0);
      |  roughnessColor=vec4(0.0,0.0,0.0,1.0);
      |  if(LIGHT_EMISSIVE.x>0.0){
      |    emissiveColor=mix(emissiveColor,CHANNEL_1,CHANNEL_1.w*LIGHT_EMISSIVE.y);
      |  }
      |  if(LIGHT_NORMAL.x>0.0){
      |    normalColor=mix(normalColor,CHANNEL_2,CHANNEL_2.w*LIGHT_NORMAL.y);
      |  }
      |  if(LIGHT_ROUGHNESS.x>0.0){
      |    roughnessColor=mix(roughnessColor,CHANNEL_3,CHANNEL_3.w*LIGHT_ROUGHNESS.y);
      |  }
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)
  }

  test("Lighting light") {

    val actual =
      Lighting.light.output.toOutput.code

    val expected: String =
      """
      |void light(){
      |  if(LIGHT_ACTIVE==1){
      |    vec4 lightResult=vec4(0.0);
      |    vec4 specularResult=vec4(0.0);
      |    switch(LIGHT_TYPE){
      |      case 0:
      |        lightResult=vec4(LIGHT_COLOR.xyz*LIGHT_COLOR.w,LIGHT_COLOR.w);
      |        specularResult=vec4(0.0);
      |        break;
      |      case 1:
      |        vec3 lightDir=normalize(vec3(sin(LIGHT_ROTATION),cos(LIGHT_ROTATION),0.0));
      |        lightResult=calculateLightColor(1.0);
      |        specularResult=calculateLightSpecular(1.0,lightDir,normalColor,roughnessColor);
      |        break;
      |      default:
      |        lightResult=vec4(0.0);
      |        specularResult=vec4(0.0);
      |        vec3 pixelPosition=vec3(SCREEN_COORDS,0.0);
      |        vec3 lightPosition=vec3(LIGHT_POSITION,0.0);
      |        vec3 lightDir2=normalize(lightPosition-pixelPosition);
      |        lightDir2=vec3(-lightDir2.x,lightDir2.yz);
      |        float boundedDistance=clamp(1.0-((distance(pixelPosition,lightPosition)-LIGHT_NEAR)/LIGHT_FAR),0.0,1.0);
      |        float lightAmount=0.0;
      |        switch(LIGHT_FALLOFF_TYPE){
      |          case 0:
      |            boundedDistance=1.0;
      |            lightAmount=1.0;
      |            break;
      |          case 1:
      |            lightAmount=LIGHT_INTENSITY*boundedDistance;
      |            break;
      |          case 2:
      |            lightAmount=pow(LIGHT_INTENSITY*boundedDistance,2.0);
      |            break;
      |          case 3:
      |            lightAmount=LIGHT_INTENSITY*(1.0/(distance(pixelPosition,lightPosition)-LIGHT_NEAR));
      |            break;
      |          case 4:
      |            lightAmount=LIGHT_INTENSITY*(1.0/(pow(distance(pixelPosition,lightPosition)-LIGHT_NEAR,2.0)));
      |            break;
      |          default:
      |            lightAmount=pow(LIGHT_INTENSITY*boundedDistance,2.0);
      |            break;
      |        }
      |        if(LIGHT_FAR_CUT_OFF==0){
      |          boundedDistance=1.0;
      |        }
      |        lightAmount=lightAmount*boundedDistance;
      |        float distanceToLight=distance(SCREEN_COORDS,LIGHT_POSITION);
      |        if((distanceToLight>LIGHT_NEAR)&&(LIGHT_FAR_CUT_OFF==0)||(distanceToLight<LIGHT_FAR)){
      |          if(LIGHT_TYPE==2){
      |            lightResult=calculateLightColor(lightAmount);
      |            specularResult=calculateLightSpecular(lightAmount,lightDir2,normalColor,roughnessColor);
      |          }
      |          if(LIGHT_TYPE==2){
      |            float viewingAngle=LIGHT_ANGLE;
      |            float viewingAngleBy2=viewingAngle/2.0;
      |            vec2 lookAtRelativeToLight=vec2(sin(LIGHT_ROTATION),-cos(LIGHT_ROTATION));
      |            float angleToLookAt=atan(lookAtRelativeToLight.y,lookAtRelativeToLight.x)+PI;
      |            float anglePlus=mod(angleToLookAt+viewingAngleBy2,2.0*PI);
      |            float angleMinus=mod(angleToLookAt-viewingAngleBy2,2.0*PI);
      |            vec2 pixelRelativeToLight=SCREEN_COORDS-LIGHT_POSITION;
      |            float angleToPixel=atan(pixelRelativeToLight.y,pixelRelativeToLight.x)+PI;
      |            if((anglePlus<angleMinus)&&(angleToPixel<anglePlus)||(angleToPixel>angleMinus)){
      |              lightResult=calculateLightColor(lightAmount);
      |              specularResult=calculateLightSpecular(lightAmount,lightDir2,normalColor,roughnessColor);
      |            }
      |            if((anglePlus>angleMinus)&&(angleToPixel<anglePlus)&&(angleToPixel>angleMinus)){
      |              lightResult=calculateLightColor(lightAmount);
      |              specularResult=calculateLightSpecular(lightAmount,lightDir2,normalColor,roughnessColor);
      |            }
      |          }
      |        }
      |        break;
      |    }
      |    specularAcc=specularAcc+specularResult;
      |    lightAcc=lightAcc+lightResult;
      |  }
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)
  }

  test("Lighting composite") {

    val actual =
      Lighting.composite.output.toOutput.code

    val expected: String =
      """
      |void composite(){
      |  float emmisiveAlpha=clamp((emissiveColor.x+emissiveColor.y)+emissiveColor.z,0.0,1.0);
      |  vec4 emissiveResult=vec4(emissiveColor.xyz*emmisiveAlpha,emmisiveAlpha);
      |  vec4 colorLightSpec=vec4(COLOR.xyz*(lightAcc.xyz+specularAcc.xyz),COLOR.w);
      |  COLOR=mix(colorLightSpec,emissiveResult,emissiveResult.w);
      |}
      |""".stripMargin.trim

    assertEquals(actual, expected)
  }

}
