package indigo.shared.scenegraph

import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Depth
import indigo.shared.materials.BlendMaterial

/** A layers are used to stack collections screen elements on top of one another.
  *
  * During the scene render, each layer in depth order is _blended_ into the one below it, a bit like doing a foldLeft
  * over a list. You can control how the blend is performed to create effects.
  *
  * Layer fields are all either Lists or options to denote that you _can_ have them but that it isn't necessary. Layers
  * are "monoids" which just means that they can be empty and they can be combined. It is important to note that when
  * they combine they are left bias in the case of all optional fields, which means, that if you do: a.show |+| b.hide,
  * the layer will be visible. This may look odd, and maybe it is (time will tell!), but the idea is that you can set
  * empty placeholder layers early in your scene and then add things to them, confident of the outcome.
  *
  * @param nodes
  *   @param key
  * @param magnification
  *   @param depth
  * @param visible
  *   @param blending
  */
final case class Layer(
    nodes: List[SceneNode],
    lights: List[Light],
    key: Option[BindingKey],
    magnification: Option[Int],
    depth: Option[Depth],
    visible: Option[Boolean],
    blending: Option[Blending]
) derives CanEqual {

  def |+|(other: Layer): Layer =
    this.copy(
      nodes = nodes ++ other.nodes,
      key = key.orElse(other.key),
      magnification = magnification.orElse(other.magnification),
      depth = depth.orElse(other.depth),
      visible = visible.orElse(other.visible),
      blending = blending.orElse(other.blending)
    )
  def combine(other: Layer): Layer =
    this |+| other

  def withNodes(newNodes: List[SceneNode]): Layer =
    this.copy(nodes = newNodes)
  def withNodes(newNodes: SceneNode*): Layer =
    withNodes(newNodes.toList)
  def addNodes(moreNodes: List[SceneNode]): Layer =
    withNodes(nodes ++ moreNodes)
  def addNodes(moreNodes: SceneNode*): Layer =
    addNodes(moreNodes.toList)
  def ++(moreNodes: List[SceneNode]): Layer =
    addNodes(moreNodes)

  def noLights: Layer =
    this.copy(lights = Nil)

  def withLights(newLights: Light*): Layer =
    withLights(newLights.toList)
  def withLights(newLights: List[Light]): Layer =
    this.copy(lights = newLights)

  def addLights(newLights: Light*): Layer =
    addLights(newLights.toList)
  def addLights(newLights: List[Light]): Layer =
    withLights(lights ++ newLights)

  def withMagnification(level: Int): Layer =
    this.copy(magnification = Option(Math.max(1, Math.min(256, level))))

  def withKey(newKey: BindingKey): Layer =
    this.copy(key = Option(newKey))

  def withDepth(newDepth: Depth): Layer =
    this.copy(depth = Option(newDepth))

  def withVisibility(isVisible: Boolean): Layer =
    this.copy(visible = Option(isVisible))

  def show: Layer =
    withVisibility(true)

  def hide: Layer =
    withVisibility(false)

  def withBlending(newBlending: Blending): Layer =
    this.copy(blending = Option(newBlending))
  def withEntityBlend(newBlend: Blend): Layer =
    this.copy(blending = blending.map(_.withEntityBlend(newBlend)))
  def withLayerBlend(newBlend: Blend): Layer =
    this.copy(blending = blending.map(_.withLayerBlend(newBlend)))
  def withBlendMaterial(newBlendMaterial: BlendMaterial): Layer =
    this.copy(blending = blending.map(_.withBlendMaterial(newBlendMaterial)))
  def modifyBlending(modifier: Blending => Blending): Layer =
    this.copy(blending = blending.map(modifier))
}

object Layer {

  def empty: Layer =
    Layer(Nil, Nil, None, None, None, None, None)

  def apply(key: BindingKey): Layer =
    Layer(Nil, Nil, Option(key), None, None, None, None)

  def apply(nodes: SceneNode*): Layer =
    Layer(nodes.toList, Nil, None, None, None, None, None)

  def apply(nodes: List[SceneNode]): Layer =
    Layer(nodes, Nil, None, None, None, None, None)

  def apply(key: BindingKey, nodes: List[SceneNode]): Layer =
    Layer(nodes, Nil, Option(key), None, None, None, None)

  def apply(key: BindingKey, nodes: SceneNode*): Layer =
    Layer(nodes.toList, Nil, Option(key), None, None, None, None)

  def apply(key: BindingKey, magnification: Int, depth: Depth): Layer =
    Layer(Nil, Nil, Option(key), Option(magnification), Option(depth), None, None)

  def apply(key: BindingKey, magnification: Int, depth: Depth, nodes: List[SceneNode]): Layer =
    Layer(nodes.toList, Nil, Option(key), Option(magnification), Option(depth), None, None)

}
