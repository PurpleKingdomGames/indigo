package com.purplekingdomgames.shared

sealed trait AssetType
case class TextAsset(name: String, path: String) extends AssetType
case class ImageAsset(name: String, path: String) extends AssetType
