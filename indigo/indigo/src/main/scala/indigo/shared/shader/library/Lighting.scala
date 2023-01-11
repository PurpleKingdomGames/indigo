package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

object Lighting:

  final case class IndigoMaterialLightingData(
      LIGHT_EMISSIVE: highp[vec2],
      LIGHT_NORMAL: highp[vec2],
      LIGHT_ROUGHNESS: highp[vec2]
  )

  object prepare:
    @SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.var"))
    inline def shader =
      Shader[IndigoFragmentEnv & IndigoMaterialLightingData] { env =>

        @const val SCREEN_GAMMA: Float = 2.2f

        ubo[IndigoMaterialLightingData]

        var normalColor: vec4    = null
        var roughnessColor: vec4 = null
        var emissiveColor: vec4  = null
        var lightAcc: vec4       = null
        var specularAcc: vec4    = null

        // format: off
        def rotationZ(angle: Float): mat4 =
          mat4(cos(angle),  -sin(angle), 0, 0,
               sin(angle),  cos(angle),  0, 0,
               0,           0,           1, 0,
               0,           0,           0, 1)
        
        def calculateLightColor(lightAmount: Float): vec4 = {
          val color: vec4 = vec4(env.LIGHT_COLOR.xyz * lightAmount * env.LIGHT_COLOR.w, lightAmount * env.LIGHT_COLOR.w)
          val colorGammaCorrected: vec4 = pow(color, vec4(1.0f / SCREEN_GAMMA))

          colorGammaCorrected
        }

        def calculateLightSpecular(lightAmount: Float,
                            lightDir: vec3,
                            normalTexture: vec4,
                            specularTexture: vec4): vec4 = {
          val shininess = (specularTexture.x + specularTexture.y + specularTexture.z) / 3.0f

          // Normal - Convert RGB 0 to 1, into -1 to 1
          val normal: vec3 = normalize((2.0f * vec3(normalTexture.xy, 1.0f)) - 1.0f)
          val rotatedNormal: vec3 = (vec4(normal, 1.0f) * rotationZ(env.ROTATION)).xyz

          val halfVec: vec3 = vec3(0.0f, 0.0f, 1.0f)
          val lambertian: Float = max(-dot(rotatedNormal, lightDir), 0.0f)
          val reflection: vec3 = normalize(vec3(2.0f * lambertian) * (rotatedNormal - lightDir))
          val specular: Float = (min(pow(dot(reflection, halfVec), shininess), lambertian) * lightAmount) * env.LIGHT_SPECULAR.w

          vec4(env.LIGHT_SPECULAR.xyz * specular, specular)
        }

        def prepare: Unit =
          // Texture order: albedo, emissive, normal, roughness

          // Initialise values
          lightAcc = vec4(0.0f, 0.0f, 0.0f, 1.0f)
          specularAcc = vec4(0.0f)
          emissiveColor = vec4(0.0f, 0.0f, 0.0f, 1.0f)
          normalColor = vec4(0.5f, 0.5f, 1.0f, 1.0f)
          roughnessColor = vec4(0.0f, 0.0f, 0.0f, 1.0f)

          if(env.LIGHT_EMISSIVE.x > 0.0f) {
            emissiveColor = mix(emissiveColor, env.CHANNEL_1, env.CHANNEL_1.w * env.LIGHT_EMISSIVE.y)
          }

          if(env.LIGHT_NORMAL.x > 0.0f) {
            normalColor = mix(normalColor, env.CHANNEL_2, env.CHANNEL_2.w * env.LIGHT_NORMAL.y)
          }

          if(env.LIGHT_ROUGHNESS.x > 0.0f) {
            roughnessColor = mix(roughnessColor, env.CHANNEL_3, env.CHANNEL_3.w * env.LIGHT_ROUGHNESS.y)
          }
      }

    val output = shader.toGLSL[Indigo]

  object light:

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    final case class LightEnv(
        var lightAcc: vec4,
        var specularAcc: vec4,
        normalColor: vec4,
        roughnessColor: vec4
    ):
      // Stub methods
      def calculateLightColor(lightAmount: Float): vec4 = vec4(0.0f)
      def calculateLightSpecular(lightAmount: Float,
                        lightDir: vec3,
                        normalTexture: vec4,
                        specularTexture: vec4): vec4 = vec4(0.0f)

    @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
    inline def shader =
      Shader[IndigoFragmentEnv & LightEnv] { env =>
        def light: Unit =
          if (env.LIGHT_ACTIVE == 1) { // light is active

            var lightResult: vec4    = vec4(0.0)
            var specularResult: vec4 = vec4(0.0)

            // 0 = ambient, 1 = direction, 2 = point, 3 = spot
            env.LIGHT_TYPE match
              case 0 =>
                lightResult = vec4(env.LIGHT_COLOR.xyz * env.LIGHT_COLOR.w, env.LIGHT_COLOR.w)
                specularResult = vec4(0.0f)

              case 1 =>
                val lightDir = normalize(vec3(sin(env.LIGHT_ROTATION), cos(env.LIGHT_ROTATION), 0.0f))
                lightResult = env.calculateLightColor(1.0f)
                specularResult = env.calculateLightSpecular(1.0f, lightDir, env.normalColor, env.roughnessColor)

              case _ =>
                lightResult = vec4(0.0f)
                specularResult = vec4(0.0f)

                val pixelPosition: vec3 = vec3(env.SCREEN_COORDS, 0.0f)
                val lightPosition: vec3 = vec3(env.LIGHT_POSITION, 0.0f)
                var lightDir2: vec3 = normalize(lightPosition - pixelPosition)
                lightDir2 = vec3(-lightDir2.x, lightDir2.yz)

                var boundedDistance = clamp(1.0f - ((distance(pixelPosition, lightPosition) - env.LIGHT_NEAR) / env.LIGHT_FAR), 0.0f, 1.0f)
                var lightAmount: Float = 0.0f

                // 0 = none, 1 = smooth linear, 2 = smooth quadtratic, 3 = linear, 4 = quadratic
                env.LIGHT_FALLOFF_TYPE match
                  case 0 =>
                    // None
                    boundedDistance = 1.0f
                    lightAmount = 1.0f

                  case 1 =>
                    // Smooth Linear
                    lightAmount = env.LIGHT_INTENSITY * boundedDistance

                  case 2 =>
                    // Smooth Quadratic
                    lightAmount = pow(env.LIGHT_INTENSITY * boundedDistance, 2.0f)

                  case 3 =>
                    // Linear (inverse-linear)
                    lightAmount = env.LIGHT_INTENSITY * (1.0f / (distance(pixelPosition, lightPosition) - env.LIGHT_NEAR))

                  case 4 =>
                    // Quadratic
                    lightAmount = env.LIGHT_INTENSITY * (1.0f / pow((distance(pixelPosition, lightPosition) - env.LIGHT_NEAR), 2.0f))

                  case _ =>
                    // Smooth Quadratic
                    lightAmount = pow(env.LIGHT_INTENSITY * boundedDistance, 2.0f)
                

                if env.LIGHT_FAR_CUT_OFF == 0 then
                  boundedDistance = 1.0f // Light attenuates forever.

                lightAmount = lightAmount * boundedDistance

                val distanceToLight: Float = distance(env.SCREEN_COORDS, env.LIGHT_POSITION)

                if distanceToLight > env.LIGHT_NEAR && (env.LIGHT_FAR_CUT_OFF == 0 || distanceToLight < env.LIGHT_FAR) then {
                  // Point light
                  if env.LIGHT_TYPE == 2 then
                    lightResult = env.calculateLightColor(lightAmount)
                    specularResult = env.calculateLightSpecular(lightAmount, lightDir2, env.normalColor, env.roughnessColor)

                  // Spot light
                  if env.LIGHT_TYPE == 2 then
                    val viewingAngle = env.LIGHT_ANGLE
                    val viewingAngleBy2 = viewingAngle / 2.0f
                    val lookAtRelativeToLight = vec2(sin(env.LIGHT_ROTATION), -cos(env.LIGHT_ROTATION))
                    val angleToLookAt = atan(lookAtRelativeToLight.y, lookAtRelativeToLight.x) + env.PI
                    val anglePlus = mod(angleToLookAt + viewingAngleBy2, 2.0f * env.PI)
                    val angleMinus = mod(angleToLookAt - viewingAngleBy2, 2.0f * env.PI)

                    val pixelRelativeToLight = env.SCREEN_COORDS - env.LIGHT_POSITION
                    val angleToPixel = atan(pixelRelativeToLight.y, pixelRelativeToLight.x) + env.PI

                    if anglePlus < angleMinus && (angleToPixel < anglePlus || angleToPixel > angleMinus) then
                      lightResult = env.calculateLightColor(lightAmount)
                      specularResult = env.calculateLightSpecular(lightAmount, lightDir2, env.normalColor, env.roughnessColor)

                    if anglePlus > angleMinus && (angleToPixel < anglePlus && angleToPixel > angleMinus) then
                      lightResult = env.calculateLightColor(lightAmount)
                      specularResult = env.calculateLightSpecular(lightAmount, lightDir2, env.normalColor, env.roughnessColor)
                }

            env.specularAcc = env.specularAcc + specularResult
            env.lightAcc = env.lightAcc + lightResult
          }
      }

    val output = shader.toGLSL[Indigo]

  object composite:
    final case class CompEnv(emissiveColor: vec4, lightAcc: vec4, specularAcc: vec4)

    inline def shader =
      Shader[IndigoFragmentEnv & CompEnv] { env =>
        def composite: vec4 =
          val emmisiveAlpha: Float = clamp(env.emissiveColor.x + env.emissiveColor.y + env.emissiveColor.z, 0.0f, 1.0f)
          val emissiveResult: vec4 = vec4(env.emissiveColor.xyz * emmisiveAlpha, emmisiveAlpha)
          val colorLightSpec: vec4 = vec4(env.COLOR.xyz * (env.lightAcc.xyz + env.specularAcc.xyz), env.COLOR.w)

          mix(colorLightSpec, emissiveResult, emissiveResult.w)
      }

    val output = 
      shader.toGLSL[Indigo]
