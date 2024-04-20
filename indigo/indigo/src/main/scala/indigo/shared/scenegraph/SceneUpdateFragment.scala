package indigo.shared.scenegraph

import indigo.BindingKey
import indigo.shared.collections.Batch
import indigo.shared.materials.BlendMaterial

import annotation.targetName

/** A description of what the engine should next present to the player.
  *
  * SceneUpdateFragments are predicatably composable, so you can make a scene in pieces and then combine them all at the
  * end.
  *
  * Note that a SceneUpdateFragment represents what is to happen next. It is not a diff. If you remove a sprite from the
  * definition it will not be drawn.
  *
  * @param layers
  *   The layers game elements are placed on.
  * @param lights
  *   Dynamic lights.
  * @param audio
  *   Background audio.
  * @param blendMaterial
  *   Optional blend material that describes how to render the scene to the screen.
  * @param cloneBlanks
  *   A list of elements that will be referenced by clones in the main layers.
  * @param camera
  *   Scene level camera enabling pan and zoom.
  */
final case class SceneUpdateFragment(
    layers: Batch[LayerEntry],
    lights: Batch[Light],
    audio: Option[SceneAudio],
    blendMaterial: Option[BlendMaterial],
    cloneBlanks: Batch[CloneBlank],
    camera: Option[Camera]
) derives CanEqual:
  import Batch.*

  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def addLayer(newLayer: LayerEntry): SceneUpdateFragment =
    SceneUpdateFragment.insertLayer(this, newLayer)
  def addLayer(newLayer: Layer): SceneUpdateFragment =
    SceneUpdateFragment.insertLayer(this, LayerEntry.Untagged(newLayer))
  def addLayer(key: BindingKey, newLayer: Layer): SceneUpdateFragment =
    SceneUpdateFragment.insertLayer(this, LayerEntry.Tagged(key, newLayer))
  def addLayer(keyAndLayer: (BindingKey, Layer)): SceneUpdateFragment =
    SceneUpdateFragment.insertLayer(this, LayerEntry.Tagged(keyAndLayer._1, keyAndLayer._2))
  def addLayer(maybeKey: Option[BindingKey], newLayer: Layer): SceneUpdateFragment =
    SceneUpdateFragment.insertLayer(this, LayerEntry(maybeKey, newLayer))
  @targetName("addLayer_maybe_key_and_layer")
  def addLayer(maybeKeyAndLayer: (Option[BindingKey], Layer)): SceneUpdateFragment =
    SceneUpdateFragment.insertLayer(this, LayerEntry(maybeKeyAndLayer._1, maybeKeyAndLayer._2))

  def addLayer(nodes: SceneNode*): SceneUpdateFragment =
    addLayer(nodes.toBatch)
  def addLayer(nodes: Batch[SceneNode]): SceneUpdateFragment =
    SceneUpdateFragment.insertLayer(this, LayerEntry(Layer(nodes)))

  def addLayers(newLayers: Batch[LayerEntry]): SceneUpdateFragment =
    this.copy(layers = newLayers.foldLeft(layers)((acc, l) => SceneUpdateFragment.mergeLayers(acc, l)))
  def addLayers(newLayers: LayerEntry*): SceneUpdateFragment =
    addLayers(newLayers.toBatch)
  @targetName("addLayers_batch_key_layer")
  def addLayers(newLayers: Batch[(BindingKey, Layer)]): SceneUpdateFragment =
    addLayers(newLayers.map(p => LayerEntry(p._1, p._2)))
  @targetName("addLayers_args_key_layer")
  def addLayers(newLayers: (BindingKey, Layer)*): SceneUpdateFragment =
    addLayers(newLayers.toBatch.map(p => LayerEntry(p._1, p._2)))
  @targetName("addLayers_batch_layer")
  def addLayers(newLayers: Batch[Layer]): SceneUpdateFragment =
    addLayers(newLayers.map(LayerEntry.Untagged.apply))
  @targetName("addLayers_args_layer")
  def addLayers(newLayers: Layer*): SceneUpdateFragment =
    addLayers(newLayers.toBatch.map(LayerEntry.Untagged.apply))

  def withLayers(layers: Batch[LayerEntry]): SceneUpdateFragment =
    this.copy(layers = layers)
  def withLayers(layers: LayerEntry*): SceneUpdateFragment =
    withLayers(layers.toBatch)
  @targetName("withLayers_batch_key_layer")
  def withLayers(layers: Batch[(BindingKey, Layer)]): SceneUpdateFragment =
    withLayers(layers.map(LayerEntry.apply))
  @targetName("withLayers_args_key_layer")
  def withLayers(layers: (BindingKey, Layer)*): SceneUpdateFragment =
    withLayers(layers.toBatch)
  @targetName("withLayers_batch_layer")
  def withLayers(layers: Batch[Layer]): SceneUpdateFragment =
    withLayers(layers.map(LayerEntry.apply))
  @targetName("withLayers_args_layer")
  def withLayers(layers: Layer*): SceneUpdateFragment =
    withLayers(layers.toBatch.map(LayerEntry.apply))

  def mapLayers(f: LayerEntry => LayerEntry): SceneUpdateFragment =
    this.copy(layers = layers.map(_.modify(f)))

  def noLights: SceneUpdateFragment =
    this.copy(lights = Batch.empty)

  def withLights(newLights: Light*): SceneUpdateFragment =
    withLights(newLights.toBatch)

  def withLights(newLights: Batch[Light]): SceneUpdateFragment =
    this.copy(lights = newLights)

  def addLights(newLights: Light*): SceneUpdateFragment =
    addLights(newLights.toBatch)

  def addLights(newLights: Batch[Light]): SceneUpdateFragment =
    withLights(lights ++ newLights)

  def withAudio(sceneAudio: SceneAudio): SceneUpdateFragment =
    this.copy(audio = Some(sceneAudio))

  def addCloneBlanks(blanks: CloneBlank*): SceneUpdateFragment =
    addCloneBlanks(blanks.toBatch)

  def addCloneBlanks(blanks: Batch[CloneBlank]): SceneUpdateFragment =
    this.copy(cloneBlanks = cloneBlanks ++ blanks)

  def withBlendMaterial(newBlendMaterial: BlendMaterial): SceneUpdateFragment =
    this.copy(blendMaterial = Option(newBlendMaterial))
  def modifyBlendMaterial(modifier: BlendMaterial => BlendMaterial): SceneUpdateFragment =
    this.copy(blendMaterial = blendMaterial.orElse(Option(BlendMaterial.Normal)).map(modifier))

  def withMagnification(level: Int): SceneUpdateFragment =
    this.copy(
      layers = layers.map(_.withMagnification(level))
    )

  def withCamera(newCamera: Camera): SceneUpdateFragment =
    this.copy(camera = Option(newCamera))
  def modifyCamera(modifier: Camera => Camera): SceneUpdateFragment =
    this.copy(camera = Option(modifier(camera.getOrElse(Camera.default))))
  def noCamera: SceneUpdateFragment =
    this.copy(camera = None)

object SceneUpdateFragment:
  import Batch.*

  def apply(nodes: SceneNode*): SceneUpdateFragment =
    SceneUpdateFragment(nodes.toBatch)

  def apply(nodes: Batch[SceneNode]): SceneUpdateFragment =
    SceneUpdateFragment(Batch(LayerEntry(Layer(nodes))), Batch.empty, None, None, Batch.empty, None)

  def apply(maybeNode: Option[SceneNode]): SceneUpdateFragment =
    SceneUpdateFragment(
      Batch(LayerEntry(Layer(Batch.fromOption(maybeNode)))),
      Batch.empty,
      None,
      None,
      Batch.empty,
      None
    )

  def apply(key: BindingKey, layer: Layer): SceneUpdateFragment =
    SceneUpdateFragment(Batch(LayerEntry.Tagged(key, layer)), Batch.empty, None, None, Batch.empty, None)
  def apply(maybeKey: Option[BindingKey], layer: Layer): SceneUpdateFragment =
    SceneUpdateFragment(Batch(LayerEntry(layer)), Batch.empty, None, None, Batch.empty, None)

  @targetName("suf-maybe-layer-entry")
  def apply(maybeLayerEntry: Option[LayerEntry]): SceneUpdateFragment =
    val layers = maybeLayerEntry.map(Batch.apply).getOrElse(Batch.empty)
    SceneUpdateFragment(layers, Batch.empty, None, None, Batch.empty, None)
  @targetName("suf-maybe-layer")
  def apply(maybeLayer: Option[Layer]): SceneUpdateFragment =
    val layers = maybeLayer.map(l => Batch(LayerEntry(l))).getOrElse(Batch.empty)
    SceneUpdateFragment(layers, Batch.empty, None, None, Batch.empty, None)
  @targetName("suf-maybe-key-and-layer")
  def apply(maybeKeyAndLayer: Option[(BindingKey, Layer)]): SceneUpdateFragment =
    val layers = maybeKeyAndLayer.map(l => Batch(LayerEntry(l))).getOrElse(Batch.empty)
    SceneUpdateFragment(layers, Batch.empty, None, None, Batch.empty, None)

  @targetName("suf-batch-layer-entry")
  def apply(layerEntries: Batch[LayerEntry]): SceneUpdateFragment =
    SceneUpdateFragment(layerEntries, Batch.empty, None, None, Batch.empty, None)
  @targetName("suf-batch-layer")
  def apply(layers: Batch[Layer]): SceneUpdateFragment =
    SceneUpdateFragment(layers.map(LayerEntry.apply), Batch.empty, None, None, Batch.empty, None)
  @targetName("suf-batch-key-and-layer")
  def apply(keysAndLayers: Batch[(BindingKey, Layer)]): SceneUpdateFragment =
    SceneUpdateFragment(keysAndLayers.map(LayerEntry.apply), Batch.empty, None, None, Batch.empty, None)
  @targetName("suf-batch-maybe-key-and-layer")
  def apply(maybeKeysAndLayers: Batch[(Option[BindingKey], Layer)]): SceneUpdateFragment =
    SceneUpdateFragment(maybeKeysAndLayers.map(p => LayerEntry(p._1, p._2)), Batch.empty, None, None, Batch.empty, None)

  @targetName("suf-apply-repeated-layer-entry")
  def apply(layerEntries: LayerEntry*): SceneUpdateFragment =
    SceneUpdateFragment(layerEntries.toBatch)
  @targetName("suf-apply-repeated-layers")
  def apply(layers: Layer*): SceneUpdateFragment =
    SceneUpdateFragment(layers.toBatch)
  @targetName("suf-apply-repeated-keys-and-layers")
  def apply(keysAndLayers: (BindingKey, Layer)*): SceneUpdateFragment =
    SceneUpdateFragment(keysAndLayers.toBatch)
  @targetName("suf-apply-repeated-maybe-keys-and-layers")
  def apply(maybeKeysAndLayers: (Option[BindingKey], Layer)*): SceneUpdateFragment =
    SceneUpdateFragment(maybeKeysAndLayers.toBatch)

  val empty: SceneUpdateFragment =
    SceneUpdateFragment(Batch.empty[LayerEntry])

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      b.layers.foldLeft(a.layers) { case (als, bl) => mergeLayers(als, bl) },
      a.lights ++ b.lights,
      b.audio.orElse(a.audio),
      b.blendMaterial.orElse(a.blendMaterial),
      a.cloneBlanks ++ b.cloneBlanks,
      b.camera.orElse(a.camera)
    )

  private[scenegraph] def mergeLayers(layers: Batch[LayerEntry], layer: LayerEntry): Batch[LayerEntry] =
    layer match
      case l @ LayerEntry.Untagged(_) =>
        layers :+ layer

      case LayerEntry.Tagged(t, l) if layers.exists(_.hasTag(t)) =>
        layers.map {
          case x: LayerEntry.Untagged =>
            x

          case x @ LayerEntry.Tagged(k, ll) if t == k =>
            val newLayer =
              (ll, l) match
                case (a: Layer.Stack, b: Layer.Content)   => a.append(b)
                case (a: Layer.Stack, b: Layer.Stack)     => a ++ b
                case (a: Layer.Content, b: Layer.Content) => a |+| b
                case (a: Layer.Content, b: Layer.Stack)   => a :: b

            LayerEntry.Tagged(t, newLayer)

          case x: LayerEntry.Tagged =>
            x
        }

      case LayerEntry.Tagged(_, _) =>
        layers :+ layer

  def insertLayer(suf: SceneUpdateFragment, layer: LayerEntry): SceneUpdateFragment =
    suf.copy(layers = mergeLayers(suf.layers, layer))
