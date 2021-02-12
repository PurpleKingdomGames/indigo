package indigo.shared.scenegraph

import indigo.shared.datatypes.RGBA

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
    ambientLight: RGBA,
    lights: List[Light],
    audio: SceneAudio,
    screenEffects: ScreenEffects,
    cloneBlanks: List[CloneBlank]
) {
  def |+|(other: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment.append(this, other)

  def addLayer(newLayer: Layer): SceneUpdateFragment =
    this.copy(layers = SceneUpdateFragment.addLayer(layers, newLayer))

  def addLayer(nodes: SceneGraphNode*): SceneUpdateFragment =
    addLayer(nodes.toList)
  def addLayer(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    this.copy(layers = SceneUpdateFragment.addLayer(layers, Layer(nodes.toList)))

  def addLayers(newLayers: Layer*): SceneUpdateFragment =
    addLayers(newLayers.toList)
  def addLayers(newLayers: List[Layer]): SceneUpdateFragment =
    this.copy(layers = newLayers.foldLeft(layers)((acc, l) => SceneUpdateFragment.addLayer(acc, l)))

  def withAmbientLight(light: RGBA): SceneUpdateFragment =
    this.copy(ambientLight = light)

  def withAmbientLightAmount(amount: Double): SceneUpdateFragment =
    this.copy(ambientLight = ambientLight.withAmount(amount))

  def withAmbientLightTint(r: Double, g: Double, b: Double): SceneUpdateFragment =
    withAmbientLight(RGBA(r, g, b, 1))

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

  def withColorOverlay(overlay: RGBA): SceneUpdateFragment =
    this.copy(screenEffects = ScreenEffects(overlay, overlay))

  def withGameColorOverlay(overlay: RGBA): SceneUpdateFragment =
    this.copy(screenEffects = screenEffects.withGameColorOverlay(overlay))

  def withUiColorOverlay(overlay: RGBA): SceneUpdateFragment =
    this.copy(screenEffects = screenEffects.withUiColorOverlay(overlay))

  def withMagnification(level: Int): SceneUpdateFragment =
    this.copy(
      layers = layers.map(_.withMagnification(level))
    )
}
object SceneUpdateFragment {

  def apply(nodes: SceneGraphNode*): SceneUpdateFragment =
    SceneUpdateFragment(nodes.toList)

  def apply(nodes: List[SceneGraphNode]): SceneUpdateFragment =
    SceneUpdateFragment(List(Layer(nodes)), RGBA.None, Nil, SceneAudio.None, ScreenEffects.None, Nil)

  def apply(layer: Layer): SceneUpdateFragment =
    SceneUpdateFragment(List(layer), RGBA.None, Nil, SceneAudio.None, ScreenEffects.None, Nil)

  val empty: SceneUpdateFragment =
    SceneUpdateFragment(Nil, RGBA.None, Nil, SceneAudio.None, ScreenEffects.None, Nil)

  def append(a: SceneUpdateFragment, b: SceneUpdateFragment): SceneUpdateFragment =
    SceneUpdateFragment(
      a.layers ++ b.layers,
      a.ambientLight + b.ambientLight,
      a.lights ++ b.lights,
      a.audio |+| b.audio,
      a.screenEffects |+| b.screenEffects,
      a.cloneBlanks ++ b.cloneBlanks
    )

  def addLayer(layers: List[Layer], layer: Layer): List[Layer] =
    if (layer.key.isDefined && layers.exists(_.key == layer.key))
      layers.map(l => if (l.key == layer.key) l.addNodes(layer.nodes) else l)
    else layers :+ layer
}
