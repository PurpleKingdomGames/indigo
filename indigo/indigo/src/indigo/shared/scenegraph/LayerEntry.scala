package indigo.shared.scenegraph

import indigo.shared.collections.Batch

/** Layer entries are holders for Layers, that can either be tagged or untagged. If a layer entry is tagged with a
  * `LayerKey`, then if two SceneUpdateFragements are merged together, two entries will the same layerKey will be
  * combined at the depth of the original.
  */
enum LayerEntry:
  def layer: Layer

  case NoKey(layer: Layer)
  case Keyed(layerKey: LayerKey, layer: Layer)

  def hasKey(layerKey: LayerKey): Boolean =
    this match
      case _: LayerEntry.NoKey => false
      case l: LayerEntry.Keyed => l.layerKey == layerKey

  def giveKey: Option[LayerKey] =
    this match
      case NoKey(_)           => None
      case Keyed(layerKey, _) => Option(layerKey)

  def withKey(newKey: LayerKey): LayerEntry =
    LayerEntry.Keyed(newKey, this.layer)

  def withLayer(newLayer: Layer): LayerEntry =
    this match
      case l: LayerEntry.NoKey => l.copy(layer = newLayer)
      case l: LayerEntry.Keyed => l.copy(layer = newLayer)

  def modify(f: LayerEntry => LayerEntry): LayerEntry =
    f(this)
  def modifyLayer(pf: PartialFunction[Layer, Layer]): LayerEntry =
    this match
      case l: LayerEntry.NoKey => l.copy(layer = layer.modify(pf))
      case l: LayerEntry.Keyed => l.copy(layer = layer.modify(pf))

  /** Apply a magnification to this layer entry's layer, and all it's child layers.
    *
    * @param level
    */
  def withMagnificationForAll(level: Int): LayerEntry =
    this.modifyLayer(_.withMagnificationForAll(level))

  def toBatch: Batch[Layer.Content] =
    layer.toBatch

object LayerEntry:

  def apply(layerKey: LayerKey, layer: Layer): LayerEntry =
    LayerEntry.Keyed(layerKey, layer)

  def apply(maybeKey: Option[LayerKey], layer: Layer): LayerEntry =
    maybeKey.fold(LayerEntry(layer))(layerKey => LayerEntry(layerKey, layer))

  def apply(keyAndLayer: (LayerKey, Layer)): LayerEntry =
    LayerEntry.Keyed(keyAndLayer._1, keyAndLayer._2)

  def apply(layer: Layer): LayerEntry =
    LayerEntry.NoKey(layer)
