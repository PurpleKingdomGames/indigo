package com.purplekingdomgames.indigo.gameengine.assets

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{FontInfo, FontKey}

import scala.collection.mutable

object FontRegister {

  private val fontRegistry: mutable.HashMap[FontKey, FontInfo] = mutable.HashMap()

  private[gameengine] def register(fontInfo: FontInfo): Unit = {
    fontRegistry.update(fontInfo.fontKey, fontInfo)
    ()
  }

  def findByFontKey(fontKey: FontKey): Option[FontInfo] =
    fontRegistry.get(fontKey)

}
