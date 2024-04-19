package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey

/** Layer entries are holders for Layers, that can either be tagged or untagged. If a layer entry is tagged with a
  * `BindingKey`, then if two SceneUpdateFragements are merged together, two entries will the same tag will be combined
  * at the depth of the original.
  */
enum LayerEntry:
  def layer: Layer

  case Untagged(layer: Layer)
  case Tagged(tag: BindingKey, layer: Layer)

  def hasTag(tag: BindingKey): Boolean =
    this match
      case _: LayerEntry.Untagged => false
      case l: LayerEntry.Tagged   => l.tag == tag

  def giveTag: Option[BindingKey] =
    this match
      case Untagged(_)    => None
      case Tagged(tag, _) => Option(tag)

  def withTag(newKey: BindingKey): LayerEntry =
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

  def toBatch: Batch[Layer.Content] =
    layer.toBatch

object LayerEntry:

  def apply(tag: BindingKey, layer: Layer): LayerEntry =
    LayerEntry.Tagged(tag, layer)

  def apply(maybeKey: Option[BindingKey], layer: Layer): LayerEntry =
    maybeKey.fold(LayerEntry(layer))(tag => LayerEntry(tag, layer))

  def apply(keyAndLayer: (BindingKey, Layer)): LayerEntry =
    LayerEntry.Tagged(keyAndLayer._1, keyAndLayer._2)

  def apply(layer: Layer): LayerEntry =
    LayerEntry.Untagged(layer)
