package indigo.shared.shader.library

import indigo.shared.shader.library.IndigoUV.*
import ultraviolet.syntax.*

import scala.annotation.nowarn

object Clip:

  trait Env extends VertexEnvReference {
    val CLIP_SHEET_FRAME_COUNT: highp[Float]    = 0.0f
    val CLIP_SHEET_FRAME_DURATION: highp[Float] = 0.0f
    val CLIP_SHEET_WRAP_AT: highp[Float]        = 0.0f
    val CLIP_SHEET_ARRANGEMENT: highp[Float]    = 0.0f
    val CLIP_SHEET_START_OFFSET: highp[Float]   = 0.0f
    val CLIP_PLAY_DIRECTION: highp[Float]       = 0.0f
    val CLIP_PLAYMODE_START_TIME: highp[Float]  = 0.0f
    val CLIP_PLAYMODE_TIMES: highp[Float]       = 0.0f
  }
  object Env:
    val reference: Env = new Env {}

  case class IndigoClipData(
      CLIP_SHEET_FRAME_COUNT: highp[Float],
      CLIP_SHEET_FRAME_DURATION: highp[Float],
      CLIP_SHEET_WRAP_AT: highp[Float],
      CLIP_SHEET_ARRANGEMENT: highp[Float], // 0 = horizontal, 1 = vertical
      CLIP_SHEET_START_OFFSET: highp[Float],
      CLIP_PLAY_DIRECTION: highp[Float], // 0 = forward, 1 = backward, 2 = ping pong
      CLIP_PLAYMODE_START_TIME: highp[Float],
      CLIP_PLAYMODE_TIMES: highp[Float]
  )

  @nowarn("msg=unused")
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def vertex =
    Shader[Env] { env =>
      ubo[IndigoClipData]

      def calcCurrentFrame(clipTotalTime: Float): Float = {
        val t: Float =
          val tt = max(env.TIME - env.CLIP_PLAYMODE_START_TIME, 0.0f)
          if env.CLIP_PLAYMODE_TIMES.toInt > 0 then
            min(tt, (clipTotalTime * env.CLIP_PLAYMODE_TIMES) - (env.CLIP_SHEET_FRAME_DURATION * 0.5f))
          else tt

        floor(
          mod(t / env.CLIP_SHEET_FRAME_DURATION, clipTotalTime / env.CLIP_SHEET_FRAME_DURATION)
        ) + env.CLIP_SHEET_START_OFFSET
      }

      def vertex(v: vec4): vec4 = {
        val direction: Int =
          val d = round(env.CLIP_PLAY_DIRECTION).toInt
          // Can't ping pong if there aren't enough frames.
          if d >= 2 && env.CLIP_SHEET_FRAME_COUNT.toInt <= 2 then 1 else d

        var clipTotalTime: Float = 0.0f
        var currentFrame: Float  = 0.0f

        // 0 = forward, 1 = backward, 2 = ping pong, 3 = smooth ping pong
        direction match
          case 0 =>
            clipTotalTime = env.CLIP_SHEET_FRAME_COUNT * env.CLIP_SHEET_FRAME_DURATION
            currentFrame = calcCurrentFrame(clipTotalTime)

          case 1 =>
            clipTotalTime = env.CLIP_SHEET_FRAME_COUNT * env.CLIP_SHEET_FRAME_DURATION
            currentFrame = calcCurrentFrame(clipTotalTime)
            currentFrame = env.CLIP_SHEET_FRAME_COUNT - 1.0f - currentFrame

          case 2 =>
            clipTotalTime = (env.CLIP_SHEET_FRAME_COUNT + env.CLIP_SHEET_FRAME_COUNT) * env.CLIP_SHEET_FRAME_DURATION
            currentFrame = calcCurrentFrame(clipTotalTime)

            if currentFrame >= env.CLIP_SHEET_FRAME_COUNT then
              currentFrame = (env.CLIP_SHEET_FRAME_COUNT * 2.0f) - 1.0f - currentFrame

          case 3 =>
            clipTotalTime =
              (env.CLIP_SHEET_FRAME_COUNT + (env.CLIP_SHEET_FRAME_COUNT - 2.0f)) * env.CLIP_SHEET_FRAME_DURATION
            currentFrame = calcCurrentFrame(clipTotalTime)

            if currentFrame >= env.CLIP_SHEET_FRAME_COUNT then
              currentFrame = (env.CLIP_SHEET_FRAME_COUNT * 2.0f) - 1.0f - (currentFrame + 1.0f)

          case _ =>
            clipTotalTime = 0.0f
            currentFrame = 0.0f

        var x: Float = 0.0f
        var y: Float = 0.0f

        val arrangement: Int = round(env.CLIP_SHEET_ARRANGEMENT).toInt

        // 0 = horizontal, 1 = vertical
        arrangement match
          case 0 =>
            x = mod(currentFrame, env.CLIP_SHEET_WRAP_AT)
            y = floor(currentFrame / env.CLIP_SHEET_WRAP_AT)

          case 1 =>
            x = floor(currentFrame / env.CLIP_SHEET_WRAP_AT)
            y = mod(currentFrame, env.CLIP_SHEET_WRAP_AT)

          case _ =>
            x = 0.0
            y = 0.0

        env.UV = env.UV + vec2(x, y);
        v
      }
    }
