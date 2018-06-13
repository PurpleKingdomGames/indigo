package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.runtime.Logger
import com.purplekingdomgames.shared.GameDefinition

object GameDefinitionHelper {

  def fromJson(json: String): Option[GameDefinition] =
    GameDefinition.fromJson(json) match {
      case Right(gd) => Some(gd)
      case Left(errorMessage) =>
        Logger.info(errorMessage)
        None
    }

}
