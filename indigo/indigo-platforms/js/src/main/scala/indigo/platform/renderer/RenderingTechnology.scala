package indigo.platform.renderer

sealed trait RenderingTechnology
object RenderingTechnology {
  case object WebGL1 extends RenderingTechnology
  case object WebGL2 extends RenderingTechnology
}
