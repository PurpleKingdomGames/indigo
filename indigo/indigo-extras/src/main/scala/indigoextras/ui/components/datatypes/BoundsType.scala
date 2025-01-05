package indigoextras.ui.components.datatypes

import indigoextras.ui.components.datatypes.Padding
import indigoextras.ui.datatypes.Bounds

/** Describes how a component should be sized within its parent.
  */
enum BoundsType[ReferenceData, A]:
  case Fixed(bounds: Bounds)
  case Calculated(calculate: (ReferenceData, A) => Bounds)
  case FillWidth(height: Int, padding: Padding)
  case FillHeight(width: Int, padding: Padding)
  case Fill(padding: Padding)

object BoundsType:

  def fixed[ReferenceData](bounds: Bounds): BoundsType[ReferenceData, Unit] =
    BoundsType.Fixed(bounds)
  def fixed[ReferenceData](width: Int, height: Int): BoundsType[ReferenceData, Unit] =
    BoundsType.Fixed(Bounds(width, height))

  def fillWidth[ReferenceData](height: Int, padding: Padding): BoundsType[ReferenceData, Unit] =
    BoundsType.FillWidth(height, padding)
  def fillHeight[ReferenceData](width: Int, padding: Padding): BoundsType[ReferenceData, Unit] =
    BoundsType.FillHeight(width, padding)
  def fill[ReferenceData](padding: Padding): BoundsType[ReferenceData, Unit] =
    BoundsType.Fill(padding)

  def calculated[ReferenceData, A](f: (ReferenceData, A) => Bounds): BoundsType[ReferenceData, A] =
    BoundsType.Calculated(f)

  object Calculated:
    def apply[ReferenceData](f: ReferenceData => Bounds): BoundsType[ReferenceData, Unit] =
      BoundsType.Calculated((ref, _) => f(ref))
