package indigoframework

import indigo.shared.IndigoLogger
import indigo.shared.GameDefinition
import indigo.json._

object GameDefinitionHelper {

  def fromJson(json: String): Option[GameDefinition] =
    gameDefinitionFromJson(json) match {
      case Right(gd) => Some(gd)
      case Left(errorMessage) =>
        IndigoLogger.info(errorMessage)
        None
    }

}
