package indigo.platform.renderer

import indigo.shared.ImageType
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Vector2
import indigo.shared.scenegraph.LayerKey

/** Configuration for a screen capture
  *
  * @param name
  *   The optional name of the capture
  * @param croppingRect
  *   The rectangle to crop the capture to
  * @param scale
  *   The scale to apply to the capture
  * @param excludeLayers
  *   The layers to exclude from the capture
  * @param imageType
  *   The type of image to capture
  */
final case class ScreenCaptureConfig(
    name: Option[String],
    croppingRect: Option[Rectangle],
    scale: Option[Vector2],
    excludeLayers: Batch[LayerKey],
    imageType: ImageType
) {

  /** Set the name of the capture
    *
    * @param name
    * @return
    */
  def withName(name: String): ScreenCaptureConfig =
    this.copy(name = Option(name))

  /** Set the cropping rectangle of the capture
    *
    * @param rect
    * @return
    */
  def withCrop(rect: Rectangle): ScreenCaptureConfig =
    this.copy(croppingRect = Option(rect))

  /** Set the scale of the capture
    *
    * @param scale
    * @return
    */
  def withScale(scale: Double): ScreenCaptureConfig =
    withScale(Vector2(scale, scale))

  /** Set the scale of the capture
    *
    * @param scale
    * @return
    */
  def withScale(scale: Vector2): ScreenCaptureConfig =
    this.copy(scale = Option(scale))

  /** Set the image type of the capture
    *
    * @param imageType
    * @return
    */
  def withImageType(imageType: ImageType): ScreenCaptureConfig =
    this.copy(imageType = imageType)

  /** Set the layers to exclude from the capture
    *
    * @param excludeLayers
    * @return
    */
  def withExcludeLayers(excludeLayers: Batch[LayerKey]): ScreenCaptureConfig =
    this.copy(excludeLayers = excludeLayers)

  /** Add a layer to exclude from the capture
    *
    * @param excludeLayer
    * @return
    */
  def addExcludeLayer(excludeLayer: LayerKey): ScreenCaptureConfig =
    this.copy(excludeLayers = excludeLayers :+ excludeLayer)

  /** Add layers to exclude from the capture
    *
    * @param excludeLayers
    * @return
    */
  def addExcludeLayers(excludeLayers: Batch[LayerKey]): ScreenCaptureConfig =
    this.copy(excludeLayers = this.excludeLayers ++ excludeLayers)
}

object ScreenCaptureConfig {

  /** Default configuration
    */
  val default: ScreenCaptureConfig =
    ScreenCaptureConfig(None, None, None, Batch.empty, ImageType.WEBP)

  /** Create a configuration with a name
    *
    * @param name
    * @return
    */
  def apply(name: String): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), None, None, Batch.empty, ImageType.WEBP)

  /** Create a configuration with a name and cropping rectangle
    *
    * @param name
    * @param croppingRect
    * @return
    */
  def apply(name: String, croppingRect: Rectangle): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), Some(croppingRect), None, Batch.empty, ImageType.WEBP)

  /** Create a configuration with a name and scale
    *
    * @param name
    * @param scale
    * @return
    */
  def apply(name: String, scale: Double): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), None, Some(Vector2(scale, scale)), Batch.empty, ImageType.WEBP)

  /** Create a configuration with a name and scale
    *
    * @param name
    * @param scale
    * @return
    */
  def apply(name: String, scale: Vector2): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), None, Some(scale), Batch.empty, ImageType.WEBP)

  /** Create a configuration with a name and excluded layers
    *
    * @param name
    * @param excludeLayers
    * @return
    */
  def apply(name: String, excludeLayers: Batch[LayerKey]): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), None, None, excludeLayers, ImageType.WEBP)

  /** Create a configuration with a name and image type
    *
    * @param name
    * @param imageType
    * @return
    */
  def apply(name: String, imageType: ImageType): ScreenCaptureConfig =
    ScreenCaptureConfig(Some(name), None, None, Batch.empty, imageType)

  /** Create a configuration with a cropping rectangle
    *
    * @param croppingRect
    * @return
    */
  def apply(croppingRect: Rectangle): ScreenCaptureConfig =
    ScreenCaptureConfig(None, Some(croppingRect), None, Batch.empty, ImageType.WEBP)

  /** Create a configuration with a scale
    *
    * @param scale
    * @return
    */
  def apply(scale: Double): ScreenCaptureConfig =
    ScreenCaptureConfig(None, None, Some(Vector2(scale, scale)), Batch.empty, ImageType.WEBP)

  /** Create a configuration with a scale
    *
    * @param scale
    * @return
    */
  def apply(scale: Vector2): ScreenCaptureConfig =
    ScreenCaptureConfig(None, None, Some(scale), Batch.empty, ImageType.WEBP)

  /** Create a configuration with excluded layers
    *
    * @param excludeLayers
    * @return
    */
  def apply(excludeLayers: Batch[LayerKey]): ScreenCaptureConfig =
    ScreenCaptureConfig(None, None, None, excludeLayers, ImageType.WEBP)

  /** Create a configuration with an image type
    *
    * @param imageType
    * @return
    */
  def apply(imageType: ImageType): ScreenCaptureConfig =
    ScreenCaptureConfig(None, None, None, Batch.empty, imageType)
}
