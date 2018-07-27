package com.purplekingdomgames.shared

sealed trait AssetType
object AssetType {
  case class Text(name: String, path: String)  extends AssetType
  case class Image(name: String, path: String) extends AssetType
  case class Audio(name: String, path: String) extends AssetType
}
