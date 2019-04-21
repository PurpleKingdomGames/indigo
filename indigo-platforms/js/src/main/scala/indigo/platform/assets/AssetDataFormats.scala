package indigo.platform.assets

import org.scalajs.dom
import org.scalajs.dom.html

object AssetDataFormats {

  type ImageDataFormat = html.Image
  type TextDataFormat  = String
  type AudioDataFormat = dom.AudioBuffer

}