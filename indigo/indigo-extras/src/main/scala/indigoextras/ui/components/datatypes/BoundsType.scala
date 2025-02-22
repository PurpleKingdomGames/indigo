package indigoextras.ui.components.datatypes

import indigoextras.ui.datatypes.Bounds
import indigoextras.ui.datatypes.UIContext

/** Describes how a component should be sized within its parent.
  */
enum BoundsType[ReferenceData, A]:
  case Fixed(bounds: Bounds)
  case Calculated(calculate: (UIContext[ReferenceData], A) => Bounds)
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

  def calculated[ReferenceData, A](f: (UIContext[ReferenceData], A) => Bounds): BoundsType[ReferenceData, A] =
    BoundsType.Calculated(f)

  object Calculated:
    def apply[ReferenceData](f: UIContext[ReferenceData] => Bounds): BoundsType[ReferenceData, Unit] =
      BoundsType.Calculated((context, _) => f(context))
