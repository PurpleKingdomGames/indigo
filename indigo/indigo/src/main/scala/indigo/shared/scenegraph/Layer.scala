package indigo.shared.scenegraph

import indigo.shared.collections.Batch
import indigo.shared.materials.BlendMaterial

import scala.annotation.tailrec

/** Layers are used to stack collections of renderable elements on top of one another, and then blend them into the rest
  * of the image composited so far. They are very much like layers from photo editing software.
  *
  * During the scene render, each 'Content' layer in depth order is _blended_ into the one below it, a bit like doing a
  * foldLeft over a list. You can control how the blend is performed to create effects.
  *
  * Layer stacks are purely an organisational device, and are ignored during rendering.
  */
enum Layer derives CanEqual:

  /** A 'stack' represents a group of nested layers. Stacks are purely for organisation, and are ignored at render time.
    *
    * @param layers
    *   a batch of layers to be processed.
    */
  case Stack(layers: Batch[Layer])

  /** Content layers are used to stack collections of screen elements on top of one another.
    *
    * During the scene render, each layer in order is _blended_ into the one below it, a bit like doing a foldLeft
    * over a list. You can control how the blend is performed to create effects.
    *
    * Layer fields are all either Batchs or options to denote that you _can_ have them but that it isn't necessary.
    * Layers are "monoids" which just means that they can be empty and they can be combined. It is important to note
    * that when they combine they are left bias in the case of all optional fields, which means, that if you do: a.show
    * |+| b.hide, the layer will be visible. This may look odd, and maybe it is (time will tell!), but the idea is that
    * you can set empty placeholder layers early in your scene and then add things to them, confident of the outcome.
    *
    * @param nodes
    *   Nodes to render in this layer.
    * @param lights
    *   Layer level dynamic lights
    * @param magnification
    *   Optionally set the magnification, defaults to scene magnification.
    * @param visible
    *   Optionally set the visiblity, defaults to visible
    * @param blending
    *   Optionally describes how to blend this layer onto the one below, by default, simply overlays one onto the other.
    * @param camera
    *   Optional camera specifically for this layer. If None, fallback to scene camera, or default camera.
    */
  case Content(
      nodes: Batch[SceneNode],
      lights: Batch[Light],
      magnification: Option[Int],
      visible: Option[Boolean],
      blending: Option[Blending],
      cloneBlanks: Batch[CloneBlank],
      camera: Option[Camera]
  )

  /** Apply a magnification to this layer and all it's child layers
    *
    * @param level
    */
  def withMagnificationForAll(level: Int): Layer =
    this.modify { case l: Layer.Content => l.withMagnification(level) }

  def toBatch: Batch[Layer.Content] =
    @tailrec
    def rec(remaining: Batch[Layer], acc: Batch[Layer.Content]): Batch[Layer.Content] =
      if remaining.isEmpty then acc
      else
        val h = remaining.head
        val t = remaining.tail

        h match
          case Layer.Stack(layers) =>
            rec(layers ++ t, acc)

          case l: Layer.Content =>
            rec(t, acc :+ l)

    rec(Batch(this), Batch.empty)

  def gatherCloneBlanks: Batch[CloneBlank] =
    @tailrec
    def rec(remaining: Batch[Layer], acc: Batch[CloneBlank]): Batch[CloneBlank] =
      if remaining.isEmpty then acc
      else
        val h = remaining.head
        val t = remaining.tail

        h match
          case Layer.Stack(layers) =>
            rec(layers ++ t, acc)

          case l: Layer.Content =>
            rec(t, acc ++ l.cloneBlanks)

    rec(Batch(this), Batch.empty)

object Layer:

  val empty: Layer.Content =
    Layer.Content.empty

  def apply(nodes: SceneNode*): Layer.Content =
    Layer.Content(Batch.fromSeq(nodes))

  def apply(nodes: Batch[SceneNode]): Layer.Content =
    Layer.Content(nodes)

  def apply(maybeNode: Option[SceneNode]): Layer.Content =
    Layer.Content(maybeNode)

  object Stack:

    val empty: Layer.Stack =
      Layer.Stack(Batch.empty)

    def apply(layers: Layer*): Layer.Stack =
      Layer.Stack(Batch.fromSeq(layers))

  object Content:

    val empty: Layer.Content =
      Layer.Content(Batch.empty, Batch.empty, None, None, None, Batch.empty, None)

    def apply(nodes: SceneNode*): Layer.Content =
      Layer.Content(Batch.fromSeq(nodes), Batch.empty, None, None, None, Batch.empty, None)

    def apply(nodes: Batch[SceneNode]): Layer.Content =
      Layer.Content(nodes, Batch.empty, None, None, None, Batch.empty, None)

    def apply(maybeNode: Option[SceneNode]): Layer.Content =
      Layer.Content(Batch.fromOption(maybeNode), Batch.empty, None, None, None, Batch.empty, None)

  extension (l: Layer)
    /** Modifies this layer, and then in the case of Layer.Stack subsequently modifies all child layers using the
      * partial function defined. Any layer that is not modified by the partial function is returned unchanged.
      */
    def modify(pf: PartialFunction[Layer, Layer]): Layer =
      pf.applyOrElse(l, identity[Layer]) match
        case Stack(layers) => Stack(layers.map(_.modify(pf)))
        case l             => l

  extension (ls: Layer.Stack)
    def combine(other: Layer.Stack): Layer.Stack =
      ls.copy(layers = ls.layers ++ other.layers)
    def ++(other: Layer.Stack): Layer.Stack =
      ls.combine(other)

    def append(layer: Layer): Layer.Stack =
      ls.copy(layers = ls.layers :+ layer)
    def :+(layer: Layer): Layer.Stack =
      ls.append(layer)
    def append(layers: Batch[Layer]): Layer.Stack =
      ls.copy(layers = ls.layers ++ layers)
    def ++(layers: Batch[Layer]): Layer.Stack =
      ls.append(layers)

    def ::(layer: Layer): Layer.Stack =
      ls.prepend(layer)
    def prepend(layer: Layer): Layer.Stack =
      ls.copy(layers = layer :: ls.layers)
    def cons(layer: Layer): Layer.Stack =
      ls.prepend(layer)

  def mergeContentLayers(a: Layer.Content, b: Layer.Content): Layer.Content =
    a.copy(
      a.nodes ++ b.nodes,
      a.lights ++ b.lights,
      a.magnification.orElse(b.magnification),
      a.visible.orElse(b.visible),
      a.blending.orElse(b.blending),
      a.cloneBlanks ++ b.cloneBlanks,
      a.camera.orElse(b.camera)
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

    def withCloneBlanks(blanks: Batch[CloneBlank]): Layer.Content =
      lc.copy(cloneBlanks = blanks)
    def withCloneBlanks(blanks: CloneBlank*): Layer.Content =
      lc.withCloneBlanks(Batch.fromSeq(blanks))

    def addCloneBlanks(blanks: Batch[CloneBlank]): Layer.Content =
      lc.copy(cloneBlanks = lc.cloneBlanks ++ blanks)
    def addCloneBlanks(blanks: CloneBlank*): Layer.Content =
      lc.addCloneBlanks(Batch.fromSeq(blanks))

    /** Apply a magnification to this content layer
      *
      * @param level
      */
    def withMagnification(level: Int): Layer.Content =
      lc.copy(magnification = Option(Math.max(1, Math.min(256, level))))

    def ::(stack: Layer.Stack): Layer.Stack =
      stack.prepend(lc)
    def +:(stack: Layer.Stack): Layer.Stack =
      stack.prepend(lc)
