package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.datatypes.BindingKey
import indigo.shared.datatypes.Depth
import indigo.shared.materials.BlendMaterial

/** Layers are used to stack collections of renderable elements on top of one another, and then blend them into the rest
  * of the image composited so far. They are very much like layers from photo editing software.
  *
  * During the scene render, each 'Content' layer in depth order is _blended_ into the one below it, a bit like doing a
  * foldLeft over a list. You can control how the blend is performed to create effects.
  *
  * Layer stacks are purely an organisational device, and are ignored during rendering.
  */
enum Layer derives CanEqual:

  /** Apply a magnification to this content layer, or all of the layers in this stack.
    *
    * @param level
    */
  def withMagnification(level: Int): Layer =
    this match
      case l: Stack =>
        l.copy(
          layers = l.layers.map(_.withMagnification(level))
        )

      case l: Layer.Content =>
        l.copy(magnification = Option(Math.max(1, Math.min(256, level))))

  /** A 'stack' represents a group of nested layers. Stacks are purely for organisation, and are ignored at render time.
    *
    * @param layers
    *   a batch of layers to be processed.
    */
  case Stack(key: Option[BindingKey], layers: Batch[Layer])

  extension (ls: Layer.Stack)
    def combine(other: Layer.Stack): Layer.Stack =
      ls.copy(layers = ls.layers ++ other.layers)
    def ++(other: Layer.Stack): Layer.Stack =
      ls.copy(layers = ls.layers ++ other.layers)

    def append(content: Layer.Content): Layer.Stack =
      ls.copy(layers = ls.layers :+ content)
    def :+(content: Layer.Content): Layer.Stack =
      ls.append(content)

    def prepend(content: Layer.Content): Layer.Stack =
      ls.copy(layers = content :: ls.layers)
    def cons(content: Layer.Content): Layer.Stack =
      ls.prepend(content)
    def ::(content: Layer.Content): Layer.Stack =
      ls.prepend(content)

    def withKey(newKey: BindingKey): Layer.Stack =
      ls.copy(key = Option(newKey))

  /** Content layers are used to stack collections of screen elements on top of one another.
    *
    * During the scene render, each layer in depth order is _blended_ into the one below it, a bit like doing a foldLeft
    * over a list. You can control how the blend is performed to create effects.
    *
    * Layer fields are all either Batchs or options to denote that you _can_ have them but that it isn't necessary.
    * Layers are "monoids" which just means that they can be empty and they can be combined. It is important to note
    * that when they combine they are left bias in the case of all optional fields, which means, that if you do: a.show
    * |+| b.hide, the layer will be visible. This may look odd, and maybe it is (time will tell!), but the idea is that
    * you can set empty placeholder layers early in your scene and then add things to them, confident of the outcome.
    *
    * @param key
    *   Optionally set a binding key, allows you to target specific layers when merging `SceneUpdateFragment`s.
    * @param nodes
    *   Nodes to render in this layer.
    * @param lights
    *   Layer level dynamic lights
    * @param magnification
    *   Optionally set the magnification, defaults to scene magnification.
    * @param depth
    *   Specifically set the depth, defaults to scene order.
    * @param visible
    *   Optionally set the visiblity, defaults to visible
    * @param blending
    *   Optionally describes how to blend this layer onto the one below, by default, simply overlays on onto the other.
    * @param camera
    *   Optional camera specifically for this layer. If None, fallback to scene camera, or default camera.
    */
  case Content(
      key: Option[BindingKey],
      nodes: Batch[SceneNode],
      lights: Batch[Light],
      magnification: Option[Int],
      depth: Option[Depth],
      visible: Option[Boolean],
      blending: Option[Blending],
      camera: Option[Camera]
  )

  def mergeContentLayers(a: Layer.Content, b: Layer.Content): Layer.Content =
    a.copy(
      nodes = a.nodes ++ b.nodes,
      key = a.key.orElse(b.key),
      magnification = a.magnification.orElse(b.magnification),
      depth = a.depth.orElse(b.depth),
      visible = a.visible.orElse(b.visible),
      blending = a.blending.orElse(b.blending),
      camera = a.camera.orElse(b.camera)
    )

  extension (lc: Layer.Content)
    def combine(other: Layer.Content): Layer.Content =
      mergeContentLayers(lc, other)
    def |+|(other: Layer.Content): Layer.Content =
      mergeContentLayers(lc, other)

    def withNodes(newNodes: Batch[SceneNode]): Layer.Content =
      lc.copy(nodes = newNodes)
    def withNodes(newNodes: SceneNode*): Layer.Content =
      withNodes(Batch.fromSeq(newNodes))
    def addNodes(moreNodes: Batch[SceneNode]): Layer.Content =
      withNodes(lc.nodes ++ moreNodes)
    def addNodes(moreNodes: SceneNode*): Layer.Content =
      addNodes(Batch.fromSeq(moreNodes))
    def ++(moreNodes: Batch[SceneNode]): Layer.Content =
      addNodes(moreNodes)

    def noLights: Layer.Content =
      lc.copy(lights = Batch.empty)

    def withLights(newLights: Light*): Layer.Content =
      withLights(Batch.fromSeq(newLights))
    def withLights(newLights: Batch[Light]): Layer.Content =
      lc.copy(lights = newLights)

    def addLights(newLights: Light*): Layer.Content =
      addLights(Batch.fromSeq(newLights))
    def addLights(newLights: Batch[Light]): Layer.Content =
      withLights(lc.lights ++ newLights)

    def withKey(newKey: BindingKey): Layer.Content =
      lc.copy(key = Option(newKey))

    def withDepth(newDepth: Depth): Layer.Content =
      lc.copy(depth = Option(newDepth))

    def withVisibility(isVisible: Boolean): Layer.Content =
      lc.copy(visible = Option(isVisible))

    def show: Layer.Content =
      withVisibility(true)

    def hide: Layer.Content =
      withVisibility(false)

    def withBlending(newBlending: Blending): Layer.Content =
      lc.copy(blending = Option(newBlending))
    def withEntityBlend(newBlend: Blend): Layer.Content =
      lc.copy(blending = lc.blending.orElse(Option(Blending.Normal)).map(_.withEntityBlend(newBlend)))
    def withLayerBlend(newBlend: Blend): Layer.Content =
      lc.copy(blending = lc.blending.orElse(Option(Blending.Normal)).map(_.withLayerBlend(newBlend)))
    def withBlendMaterial(newBlendMaterial: BlendMaterial): Layer.Content =
      lc.copy(blending = lc.blending.orElse(Option(Blending.Normal)).map(_.withBlendMaterial(newBlendMaterial)))
    def modifyBlending(modifier: Blending => Blending): Layer.Content =
      lc.copy(blending = lc.blending.orElse(Option(Blending.Normal)).map(modifier))

    def withCamera(newCamera: Camera): Layer.Content =
      lc.copy(camera = Option(newCamera))
    def modifyCamera(modifier: Camera => Camera): Layer.Content =
      lc.copy(camera = Option(modifier(lc.camera.getOrElse(Camera.default))))
    def noCamera: Layer.Content =
      lc.copy(camera = None)

object Layer:

  val empty: Layer.Content =
    Layer.Content.empty

  def apply(key: BindingKey): Layer.Content =
    Layer.Content(key)

  def apply(nodes: SceneNode*): Layer.Content =
    Layer.Content(Batch.fromSeq(nodes))

  def apply(nodes: Batch[SceneNode]): Layer.Content =
    Layer.Content(nodes)

  def apply(maybeNode: Option[SceneNode]): Layer.Content =
    Layer.Content(maybeNode)

  def apply(key: BindingKey, nodes: Batch[SceneNode]): Layer.Content =
    Layer.Content(key, nodes)

  def apply(key: BindingKey, nodes: SceneNode*): Layer.Content =
    Layer.Content(key, Batch.fromSeq(nodes))

  def apply(key: BindingKey, magnification: Int, depth: Depth): Layer.Content =
    Layer.Content(key, magnification, depth)

  def apply(key: BindingKey, magnification: Int, depth: Depth, nodes: Batch[SceneNode]): Layer.Content =
    Layer.Content(key, magnification, depth, nodes)

  object Stack:
    val empty: Layer.Stack =
      Layer.Stack(None, Batch.empty)

    def apply(key: BindingKey): Layer.Stack =
      Layer.Stack(Option(key), Batch.empty)

    def apply(layers: Layer*): Layer.Stack =
      Layer.Stack(None, Batch.fromSeq(layers))

    def apply(layers: Batch[Layer]): Layer.Stack =
      Layer.Stack(None, layers)

    def apply(key: BindingKey, layers: Batch[Layer]): Layer.Stack =
      Layer.Stack(Option(key), layers)

    def apply(key: BindingKey, layers: Layer*): Layer.Stack =
      Layer.Stack(Option(key), Batch.fromSeq(layers))

  object Content:

    val empty: Layer.Content =
      Layer.Content(None, Batch.empty, Batch.empty, None, None, None, None, None)

    def apply(key: BindingKey): Layer.Content =
      Layer.Content(Option(key), Batch.empty, Batch.empty, None, None, None, None, None)

    def apply(nodes: SceneNode*): Layer.Content =
      Layer.Content(None, Batch.fromSeq(nodes), Batch.empty, None, None, None, None, None)

    def apply(nodes: Batch[SceneNode]): Layer.Content =
      Layer.Content(None, nodes, Batch.empty, None, None, None, None, None)

    def apply(maybeNode: Option[SceneNode]): Layer.Content =
      Layer.Content(None, Batch.fromOption(maybeNode), Batch.empty, None, None, None, None, None)

    def apply(key: BindingKey, nodes: Batch[SceneNode]): Layer.Content =
      Layer.Content(Option(key), nodes, Batch.empty, None, None, None, None, None)

    def apply(key: BindingKey, nodes: SceneNode*): Layer.Content =
      Layer.Content(Option(key), Batch.fromSeq(nodes), Batch.empty, None, None, None, None, None)

    def apply(key: BindingKey, magnification: Int, depth: Depth): Layer.Content =
      Layer.Content(Option(key), Batch.empty, Batch.empty, Option(magnification), Option(depth), None, None, None)

    def apply(key: BindingKey, magnification: Int, depth: Depth, nodes: Batch[SceneNode]): Layer.Content =
      Layer.Content(Option(key), nodes, Batch.empty, Option(magnification), Option(depth), None, None, None)
