package indigo.shared.display

import indigo.shared.datatypes.AmbientLight
import indigo.shared.AsString
import indigo.shared.EqualTo

final class Displayable(val game: List[DisplayObject], val lighting: List[DisplayObject], val ui: List[DisplayObject], val ambientLight: AmbientLight)

object Displayable {

  implicit val show: AsString[Displayable] = {
    val sd = implicitly[AsString[List[DisplayObject]]]
    val sa = implicitly[AsString[AmbientLight]]

    AsString.create { v =>
      s"Displayable(${sd.show(v.game)}, ${sd.show(v.lighting)}, ${sd.show(v.ui)}, ${sa.show(v.ambientLight)})"
    }
  }

  implicit val eq: EqualTo[Displayable] = {
    val ed = implicitly[EqualTo[List[DisplayObject]]]
    val ea = implicitly[EqualTo[AmbientLight]]

    EqualTo.create { (a, b) =>
      ed.equal(a.game, b.game) &&
      ed.equal(a.lighting, b.lighting) &&
      ed.equal(a.ui, b.ui) &&
      ea.equal(a.ambientLight, b.ambientLight)
    }
  }

  def apply(game: List[DisplayObject], lighting: List[DisplayObject], ui: List[DisplayObject], ambientLight: AmbientLight): Displayable =
    new Displayable(game, lighting, ui, ambientLight)

}
