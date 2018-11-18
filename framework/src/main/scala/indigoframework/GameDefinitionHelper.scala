package indigoframework

import indigo.runtime.IndigoLogger
import indigo.shared.GameDefinition

object GameDefinitionHelper {

  def fromJson(json: String): Option[GameDefinition] =
    GameDefinition.fromJson(json) match {
      case Right(gd) => Some(gd)
      case Left(errorMessage) =>
        IndigoLogger.info(errorMessage)
        None
    }

}
