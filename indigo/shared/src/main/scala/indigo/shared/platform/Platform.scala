package indigo.shared.platform

import scala.util.Try

trait Platform {

  def initialise(): Try[(Renderer, AssetMapping)]

  def tick(loop: Long => Unit): Unit

}
