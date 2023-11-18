package indigo.shared.scenegraph

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
    layers: Batch[Layer],
    lights: Batch[Light],
    audio: Option[SceneAudio],
    blendMaterial: Option[BlendMaterial],
    cloneBlanks: Batch[CloneBlank],
    camera: Option[Camera]
) derives CanEqual:
  import Batch.*

  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def addLayer(newLayer: Layer): SceneUpdateFragment =
    this.copy(layers = SceneUpdateFragment.addLayer(layers, newLayer))

  def addLayer(nodes: SceneNode*): SceneUpdateFragment =
    addLayer(nodes.toBatch)
  def addLayer(nodes: Batch[SceneNode]): SceneUpdateFragment =
    this.copy(layers = SceneUpdateFragment.addLayer(layers, Layer(nodes)))

  def addLayers(newLayers: Layer*): SceneUpdateFragment =
    addLayers(newLayers.toBatch)
  def addLayers(newLayers: Batch[Layer]): SceneUpdateFragment =
    this.copy(layers = newLayers.foldLeft(layers)((acc, l) => SceneUpdateFragment.addLayer(acc, l)))

  def withLayers(layers: Batch[Layer]): SceneUpdateFragment =
    this.copy(layers = layers)
  def withLayers(layers: Layer*): SceneUpdateFragment =
    withLayers(layers.toBatch)

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
    SceneUpdateFragment(Batch(Layer(nodes)), Batch.empty, None, None, Batch.empty, None)

  def apply(maybeNode: Option[SceneNode]): SceneUpdateFragment =
    SceneUpdateFragment(Batch(Layer(Batch.fromOption(maybeNode))), Batch.empty, None, None, Batch.empty, None)

  def apply(layer: Layer): SceneUpdateFragment =
    SceneUpdateFragment(Batch(layer), Batch.empty, None, None, Batch.empty, None)

  @targetName("suf-maybe-layer")
  def apply(maybeLayer: Option[Layer]): SceneUpdateFragment =
    val layers = maybeLayer.map(l => Batch(l)).getOrElse(Batch.empty)
    SceneUpdateFragment(layers, Batch.empty, None, None, Batch.empty, None)

  @targetName("suf-batch-layers")
  def apply(layers: Batch[Layer]): SceneUpdateFragment =
    SceneUpdateFragment(layers, Batch.empty, None, None, Batch.empty, None)

  @targetName("suf-apply-many-layers")
  def apply(layers: Layer*): SceneUpdateFragment =
    SceneUpdateFragment(layers.toBatch, Batch.empty, None, None, Batch.empty, None)

  val empty: SceneUpdateFragment =
    SceneUpdateFragment(Batch.empty, Batch.empty, None, None, Batch.empty, None)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      b.layers.foldLeft(a.layers) { case (als, bl) => addLayer(als, bl) },
      a.lights ++ b.lights,
      b.audio.orElse(a.audio),
      b.blendMaterial.orElse(a.blendMaterial),
      a.cloneBlanks ++ b.cloneBlanks,
      b.camera.orElse(a.camera)
    )

  def addLayer(layers: Batch[Layer], layer: Layer): Batch[Layer] =
    if (layer.key.isDefined && layers.exists(_.key == layer.key))
      layers.map { l =>
        if (l.key == layer.key) l |+| layer else l
      }
    else layers :+ layer
