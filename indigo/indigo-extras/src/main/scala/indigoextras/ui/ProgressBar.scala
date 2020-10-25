package indigoextras.ui

import indigo.shared.datatypes.Point
import indigo.shared.scenegraph.{Graphic, Group}

/**
  * Presents a progress bar graphic that increases horizontally as progress is made.
  * The progress dictates how much the bar graphic is cropped from the right: no progress means
  * cropped to nothing, completion means not cropped.
  *
  * @param basePosition The position of the base graphic behind the bar (top-left corner)
  * @param barPosition The position of the bar (top-left corner)
  * @param base The base graphic always drawn under the bar
  * @param bar The bar graphic, cropped according to progress
  * @param progress The proportion of progress shown from 0.0 (none) to 1.0 (complete)
  */
final case class ProgressBar(
    basePosition: Point,
    barPosition: Point,
    base: Graphic,
    bar: Graphic,
    progress: Double
) {

  /**
    * Update the progress made.
    * @param updatedProgress The new progress value
    * @return The progress bar with the updated progress value
    */
  def update(updatedProgress: Double): ProgressBar =
    this.copy(progress = updatedProgress)

  /**
    * Create a drawable representation of the current bar.
    * @return A fragment showing the progress bar, to be included in a scene
    */
  def draw: Group =
    Group(
      base.moveTo(basePosition),
      bar
        .moveTo(barPosition)
        .withCrop(0, 0, (bar.lazyBounds.width * progress).toInt, bar.lazyBounds.height)
    )
}
