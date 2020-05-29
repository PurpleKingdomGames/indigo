package indigo.platform.renderer

import indigo.shared.platform.Renderer
import indigo.shared.platform.RendererConfig
import indigo.platform.renderer.shared.LoadedTextureAsset
import indigo.platform.renderer.shared.ContextAndCanvas

import org.scalajs.dom
import org.scalajs.dom.raw.WebGLRenderingContext
import org.scalajs.dom.{Element, html, raw}
import scala.scalajs.js.Dynamic
import indigo.platform.renderer.webgl1.RendererWebGL1
import indigo.platform.renderer.webgl2.RendererWebGL2
import indigo.shared.config.RenderingTechnology
import indigo.shared.IndigoLogger

final class RendererInitialiser(renderingTechnology: RenderingTechnology) {

  def setup(config: RendererConfig, loadedTextureAssets: List[LoadedTextureAsset], canvas: html.Canvas): Renderer = {
    val (cNc, tech) = setupContextAndCanvas(canvas, config.magnification, config.antiAliasing)
    val r =
      tech match {
        case RenderingTechnology.WebGL1 =>
          new RendererWebGL1(config, loadedTextureAssets, cNc)

        case RenderingTechnology.WebGL2 =>
          new RendererWebGL2(config, loadedTextureAssets, cNc)

        case RenderingTechnology.WebGL2WithFallback =>
          new RendererWebGL2(config, loadedTextureAssets, cNc)
      }

    r.init()
    r
  }

  def createCanvas(width: Int, height: Int, parent: Element): html.Canvas =
    createNamedCanvas(width, height, "indigo", Some(parent))

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.NonUnitStatements",
      "org.wartremover.warts.AsInstanceOf",
      "org.wartremover.warts.Null",
      "org.wartremover.warts.Equals",
      "org.wartremover.warts.Var"
    )
  )
  def createNamedCanvas(width: Int, height: Int, name: String, appendToParent: Option[Element]): html.Canvas = {
    var canvas: html.Canvas = dom.document.getElementById(name).asInstanceOf[html.Canvas]

    if (canvas == null) {
      canvas = dom.document.createElement("canvas").asInstanceOf[html.Canvas]

      appendToParent match {
        case Some(parent) =>
          parent.appendChild(canvas)
        case None =>
          ()
      }

      canvas.id = name
      canvas.width = width
      canvas.height = height
    }

    canvas
  }

  private def setupContextAndCanvas(canvas: html.Canvas, magnification: Int, antiAliasing: Boolean): (ContextAndCanvas, RenderingTechnology) = {
    val (ctx, tech) = getContext(canvas, antiAliasing)

    val cNc =
      new ContextAndCanvas(
        context = ctx,
        canvas = canvas,
        magnification = magnification
      )

    (cNc, tech)
  }

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.AsInstanceOf",
      "org.wartremover.warts.IsInstanceOf",
      "org.wartremover.warts.Equals",
      "org.wartremover.warts.Null",
      "org.wartremover.warts.Throw",
      "org.wartremover.warts.Var"
    )
  )
  private def getContext(canvas: html.Canvas, antiAliasing: Boolean): (WebGLRenderingContext, RenderingTechnology) = {
    val args =
      Dynamic.literal("premultipliedAlpha" -> false, "alpha" -> false, "antialias" -> antiAliasing)

    val tech = chooseRenderingTechnology(renderingTechnology, args)

    def useWebGL1(): (WebGLRenderingContext, RenderingTechnology) = {
      val gl = (canvas.getContext("webgl", args) || canvas.getContext("experimental-webgl", args)).asInstanceOf[raw.WebGLRenderingContext]
      (gl, RenderingTechnology.WebGL1)
    }

    def useWebGL2(): (WebGLRenderingContext, RenderingTechnology) = {
      val gl2 = (canvas.getContext("webgl2", args)).asInstanceOf[raw.WebGLRenderingContext]
      (gl2, RenderingTechnology.WebGL2)
    }

    tech match {
      case Left(_)  => useWebGL1()
      case Right(_) => useWebGL2()
    }
  }

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.AsInstanceOf",
      "org.wartremover.warts.IsInstanceOf",
      "org.wartremover.warts.Equals",
      "org.wartremover.warts.Null",
      "org.wartremover.warts.Throw",
      "org.wartremover.warts.Var"
    )
  )
  private def chooseRenderingTechnology(usersPreference: RenderingTechnology, args: Dynamic): Either[RenderingTechnology.WebGL1.type, RenderingTechnology.WebGL2.type] = {
    val tempCanvas = createNamedCanvas(1, 1, "indigowebgl2test", None)

    def tryWebGL1(prefix: String): Either[RenderingTechnology.WebGL1.type, RenderingTechnology.WebGL2.type] = {
      val gl = (tempCanvas.getContext("webgl", args) || tempCanvas.getContext("experimental-webgl", args)).asInstanceOf[raw.WebGLRenderingContext]

      if (gl == null)
        throw new Exception(prefix + "This browser does not support WebGL 1.0.")
      else {
        IndigoLogger.info("Using WebGL 1.0.")
        Left(RenderingTechnology.WebGL1)
      }
    }

    usersPreference match {
      case RenderingTechnology.WebGL1 =>
        tryWebGL1("WebGL 1.0 required by indigo game. ")

      case RenderingTechnology.WebGL2 =>
        val gl2 = (tempCanvas.getContext("webgl2", args)).asInstanceOf[raw.WebGLRenderingContext]

        if (gl2 == null)
          throw new Exception("WebGL 2.0 required by indigo game. This browser does not support WebGL 2.0.")
        else if (!isWebGL2ReallySupported(gl2))
          throw new Exception("WebGL 2.0 required by indigo game. This browser claims to support WebGL 2.0, but is not fully implemented.")
        else {
          IndigoLogger.info("Using WebGL 2.0")
          Right(RenderingTechnology.WebGL2)
        }

      case RenderingTechnology.WebGL2WithFallback =>
        var gl2 = (tempCanvas.getContext("webgl2", args)).asInstanceOf[raw.WebGLRenderingContext]

        if (gl2 == null) {
          IndigoLogger.info("This browser does not support WebGL 2.0, trying WebGL 1.0.")
          tryWebGL1("")
        } else if (!isWebGL2ReallySupported(gl2)) {
          IndigoLogger.info("This browser claims to support WebGL 2.0, but it is not fully implemented. Trying WebGL 1.0.")
          gl2 = null
          tryWebGL1("")
        } else {
          IndigoLogger.info("Using WebGL 2.0.")
          Right(RenderingTechnology.WebGL2)
        }
    }
  }

  @SuppressWarnings(
    Array(
      "org.wartremover.warts.AsInstanceOf",
      "org.wartremover.warts.IsInstanceOf",
      "org.wartremover.warts.Throw",
      "org.wartremover.warts.Null",
      "org.wartremover.warts.Equals"
    )
  )
  private def isWebGL2ReallySupported(gl2: raw.WebGLRenderingContext): Boolean = {

    def testWebGL2Compatibility(param: Int, min: Int): Boolean =
      try if (gl2.getParameter(param).isInstanceOf[Int]) {
        val value = gl2.getParameter(param).asInstanceOf[Int]
        !value.toFloat.isNaN() && value >= min
      } else false
      catch {
        case _: Throwable => false
      }

    val tests = List(
      (indigo.facades.WebGL2RenderingContext.MAX_3D_TEXTURE_SIZE, 256),
      (indigo.facades.WebGL2RenderingContext.MAX_DRAW_BUFFERS, 4),
      (indigo.facades.WebGL2RenderingContext.MAX_COLOR_ATTACHMENTS, 4),
      (indigo.facades.WebGL2RenderingContext.MAX_VERTEX_UNIFORM_BLOCKS, 12),
      (indigo.facades.WebGL2RenderingContext.MAX_VERTEX_TEXTURE_IMAGE_UNITS, 16),
      (indigo.facades.WebGL2RenderingContext.MAX_FRAGMENT_INPUT_COMPONENTS, 60),
      (indigo.facades.WebGL2RenderingContext.MAX_UNIFORM_BUFFER_BINDINGS, 24),
      (indigo.facades.WebGL2RenderingContext.MAX_COMBINED_UNIFORM_BLOCKS, 24)
    )

    gl2 != null && tests.forall(t => testWebGL2Compatibility(t._1, t._2))
  }

}
