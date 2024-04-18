package indigo.shared.scenegraph

import indigo.BindingKey

enum LayerEntry:
  def layer: Layer

  case Untagged(layer: Layer)
  case Tagged(key: BindingKey, layer: Layer)

  def hasKey(key: BindingKey): Boolean =
    this match
      case _: LayerEntry.Untagged => false
      case l: LayerEntry.Tagged   => l.key == key

  def withKey(newKey: BindingKey): LayerEntry =
    LayerEntry.Tagged(newKey, this.layer)

  def withLayer(newLayer: Layer): LayerEntry =
    this match
      case l: LayerEntry.Untagged => l.copy(layer = newLayer)
      case l: LayerEntry.Tagged   => l.copy(layer = newLayer)

  def modify(f: LayerEntry => LayerEntry): LayerEntry =
    f(this)
  def modifyLayer(f: Layer => Layer): LayerEntry =
    this match
      case l: LayerEntry.Untagged => l.copy(layer = f(l.layer))
      case l: LayerEntry.Tagged   => l.copy(layer = f(l.layer))

  def withMagnification(level: Int): LayerEntry =
    this match
      case l: LayerEntry.Untagged => l.copy(layer = l.layer.withMagnification(level))
      case l: LayerEntry.Tagged   => l.copy(layer = l.layer.withMagnification(level))

object LayerEntry:

  def apply(key: BindingKey, layer: Layer): LayerEntry =
    LayerEntry.Tagged(key, layer)

  def apply(maybeKey: Option[BindingKey], layer: Layer): LayerEntry =
    maybeKey.fold(LayerEntry(layer))(key => LayerEntry(key, layer))

  def apply(keyAndLayer: (BindingKey, Layer)): LayerEntry =
    LayerEntry.Tagged(keyAndLayer._1, keyAndLayer._2)

  def apply(layer: Layer): LayerEntry =
    LayerEntry.Untagged(layer)
