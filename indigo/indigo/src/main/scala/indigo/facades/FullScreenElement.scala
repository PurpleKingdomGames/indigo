package indigo.facades

import org.scalajs.dom.raw.Element

import scala.scalajs.js

@js.native
trait FullScreenElement extends js.Object {

  def fullscreenElement: js.UndefOr[Element] = js.native

  def requestFullscreen(): js.Promise[Unit] = js.native

  def exitFullscreen(): js.Promise[Unit] = js.native

}
