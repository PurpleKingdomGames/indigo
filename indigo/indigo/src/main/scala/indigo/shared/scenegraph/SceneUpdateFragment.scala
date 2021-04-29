package indigo.shared.scenegraph

import indigo.shared.materials.BlendMaterial

/**
  * A description of what the engine should next present to the player.
  *
  * SceneUpdateFragments are predicatably composable, so you can make a scene in pieces and then combine them all at the end.
  *
  * Note that a SceneUpdateFragment represents what is to happen next. It is not a diff. If you remove a sprite from the definition it will not be drawn.
  *
  * @param layers The layers game elements are placed on.
  * @param ambientLight The scene's ambient light levels.
  * @param lights Dynamic lights.
  * @param audio Background audio.
  * @param screenEffects Effects to be applied at screen level.
  * @param cloneBlanks A list of elements that will be referenced by clones in the main layers.
  */
final case class SceneUpdateFragment(
    layers: List[Layer],
    lights: List[Light],
    audio: SceneAudio,
    blendMaterial: Option[BlendMaterial],
    cloneBlanks: List[CloneBlank]
) derives CanEqual {
  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def addLayer(newLayer: Layer): SceneUpdateFragment =
    this.copy(layers = SceneUpdateFragment.addLayer(layers, newLayer))

  def addLayer(nodes: SceneNode*): SceneUpdateFragment =
    addLayer(nodes.toList)
  def addLayer(nodes: List[SceneNode]): SceneUpdateFragment =
    this.copy(layers = SceneUpdateFragment.addLayer(layers, Layer(nodes.toList)))

  def addLayers(newLayers: Layer*): SceneUpdateFragment =
    addLayers(newLayers.toList)
  def addLayers(newLayers: List[Layer]): SceneUpdateFragment =
    this.copy(layers = newLayers.foldLeft(layers)((acc, l) => SceneUpdateFragment.addLayer(acc, l)))

  def noLights: SceneUpdateFragment =
    this.copy(lights = Nil)

  def withLights(newLights: Light*): SceneUpdateFragment =
    withLights(newLights.toList)

  def withLights(newLights: List[Light]): SceneUpdateFragment =
    this.copy(lights = newLights)

  def addLights(newLights: Light*): SceneUpdateFragment =
    addLights(newLights.toList)

  def addLights(newLights: List[Light]): SceneUpdateFragment =
    withLights(lights ++ newLights)

  def withAudio(sceneAudio: SceneAudio): SceneUpdateFragment =
    this.copy(audio = sceneAudio)

  def addCloneBlanks(blanks: CloneBlank*): SceneUpdateFragment =
    addCloneBlanks(blanks.toList)

  def addCloneBlanks(blanks: List[CloneBlank]): SceneUpdateFragment =
    this.copy(cloneBlanks = cloneBlanks ++ blanks)

  def withBlendMaterial(newBlendMaterial: BlendMaterial): SceneUpdateFragment =
    this.copy(blendMaterial = Option(newBlendMaterial))
  def modifyBlendMaterial(modifier: BlendMaterial => BlendMaterial): SceneUpdateFragment =
    this.copy(blendMaterial = blendMaterial.map(modifier))

  def withMagnification(level: Int): SceneUpdateFragment =
    this.copy(
      layers = layers.map(_.withMagnification(level))
    )
}
object SceneUpdateFragment {

  def apply(nodes: SceneNode*): SceneUpdateFragment =
    SceneUpdateFragment(nodes.toList)

  def apply(nodes: List[SceneNode]): SceneUpdateFragment =
    SceneUpdateFragment(List(Layer(nodes)), Nil, SceneAudio.None, None, Nil)

  def apply(layer: Layer): SceneUpdateFragment =
    SceneUpdateFragment(List(layer), Nil, SceneAudio.None, None, Nil)

  val empty: SceneUpdateFragment =
    SceneUpdateFragment(Nil, Nil, SceneAudio.None, None, Nil)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      b.layers.foldLeft(a.layers) { case (als, bl) => addLayer(als, bl) },
      a.lights ++ b.lights,
      a.audio |+| b.audio,
      b.blendMaterial.orElse(a.blendMaterial),
      a.cloneBlanks ++ b.cloneBlanks
    )

  def addLayer(layers: List[Layer], layer: Layer): List[Layer] =
    if (layer.key.isDefined && layers.exists(_.key == layer.key))
      layers.map { l =>
        if (l.key == layer.key) l |+| layer else l
      }
    else layers :+ layer
}
