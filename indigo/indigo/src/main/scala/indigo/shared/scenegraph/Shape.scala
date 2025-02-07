package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Circle as C
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.RGBA
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.datatypes.Size
import indigo.shared.datatypes.Stroke
import indigo.shared.datatypes.Vector2
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.LightingModel
import indigo.shared.materials.LightingModel.Lit
import indigo.shared.materials.LightingModel.Unlit
import indigo.shared.shader.ShaderData
import indigo.shared.shader.ShaderId
import indigo.shared.shader.ShaderPrimitive.*
import indigo.shared.shader.StandardShaders
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.UniformBlockName

/** Parent type for all Shapes, which are visible elements draw mathematically that require no textures. Shapes are
  * quite versitile and support different fills and stroke effects, even lighting. Due to the way strokes around shapes
  * are drawn, the corners are always rounded.
  */
sealed trait Shape[T <: Shape[?]] extends RenderNode[T] with Cloneable with SpatialModifiers[T] derives CanEqual:
  def moveTo(pt: Point): T
  def moveTo(x: Int, y: Int): T
  def withPosition(newPosition: Point): T

  def moveBy(pt: Point): T
  def moveBy(x: Int, y: Int): T

  def rotateTo(angle: Radians): T
  def rotateBy(angle: Radians): T
  def withRotation(newRotation: Radians): T

  def scaleBy(amount: Vector2): T
  def scaleBy(x: Double, y: Double): T
  def withScale(newScale: Vector2): T

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): T
  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): T

  def flipHorizontal(isFlipped: Boolean): T
  def flipVertical(isFlipped: Boolean): T
  def withFlip(newFlip: Flip): T

  def bounds: Rectangle =
    BoundaryLocator.findShapeBounds(this)

object Shape:

  /** Draws a coloured box that occupies a rectangle on the screen.
    */
  final case class Box(
      dimensions: Rectangle,
      fill: Fill,
      stroke: Stroke,
      lighting: LightingModel,
      eventHandlerEnabled: Boolean,
      eventHandler: ((Box, GlobalEvent)) => Option[GlobalEvent],
      rotation: Radians,
      scale: Vector2,
      ref: Point,
      flip: Flip,
      shaderId: Option[ShaderId]
  ) extends Shape[Box] {

    lazy val position: Point =
      dimensions.position - (stroke.width / 2)
    lazy val size: Size =
      dimensions.size + stroke.width

    def withDimensions(newDimensions: Rectangle): Box =
      this.copy(dimensions = newDimensions)

    def resize(size: Size): Box =
      this.copy(dimensions = dimensions.resize(size))

    def withFill(newFill: Fill): Box =
      this.copy(fill = newFill)
    def modifyFill(modifier: Fill => Fill): Box =
      this.copy(fill = modifier(fill))

    def withStroke(newStroke: Stroke): Box =
      this.copy(stroke = newStroke)
    def modifyStroke(modifier: Stroke => Stroke): Box =
      this.copy(stroke = modifier(stroke))

    def withStrokeColor(newStrokeColor: RGBA): Box =
      this.copy(stroke = stroke.withColor(newStrokeColor))

    def withStrokeWidth(newWidth: Int): Box =
      this.copy(stroke = stroke.withWidth(newWidth))

    def withLighting(newLighting: LightingModel): Box =
      this.copy(lighting = newLighting)
    def modifyLighting(modifier: LightingModel => LightingModel): Box =
      this.copy(lighting = modifier(lighting))

    def moveTo(pt: Point): Box =
      this.copy(dimensions = dimensions.moveTo(pt))
    def moveTo(x: Int, y: Int): Box =
      moveTo(Point(x, y))
    def withPosition(newPosition: Point): Box =
      moveTo(newPosition)

    def moveBy(pt: Point): Box =
      this.copy(dimensions = dimensions.moveBy(pt))
    def moveBy(x: Int, y: Int): Box =
      moveBy(Point(x, y))

    def rotateTo(angle: Radians): Box =
      this.copy(rotation = angle)
    def rotateBy(angle: Radians): Box =
      rotateTo(rotation + angle)
    def withRotation(newRotation: Radians): Box =
      rotateTo(newRotation)

    def scaleBy(amount: Vector2): Box =
      this.copy(scale = scale * amount)
    def scaleBy(x: Double, y: Double): Box =
      scaleBy(Vector2(x, y))
    def withScale(newScale: Vector2): Box =
      this.copy(scale = newScale)

    def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Box =
      this.copy(dimensions = dimensions.moveTo(newPosition), rotation = newRotation, scale = newScale)

    def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Box =
      transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

    def withRef(newRef: Point): Box =
      this.copy(ref = newRef)
    def withRef(x: Int, y: Int): Box =
      withRef(Point(x, y))

    def flipHorizontal(isFlipped: Boolean): Box =
      this.copy(flip = flip.withHorizontalFlip(isFlipped))
    def flipVertical(isFlipped: Boolean): Box =
      this.copy(flip = flip.withVerticalFlip(isFlipped))
    def withFlip(newFlip: Flip): Box =
      this.copy(flip = newFlip)

    def withShaderId(newShaderId: ShaderId): Box =
      this.copy(shaderId = Option(newShaderId))

    def withEventHandler(f: ((Box, GlobalEvent)) => Option[GlobalEvent]): Box =
      this.copy(eventHandler = f, eventHandlerEnabled = true)
    def onEvent(f: PartialFunction[(Box, GlobalEvent), GlobalEvent]): Box =
      withEventHandler(f.lift)
    def enableEvents: Box =
      this.copy(eventHandlerEnabled = true)
    def disableEvents: Box =
      this.copy(eventHandlerEnabled = false)
  }
  object Box {

    def apply(dimensions: Rectangle, fill: Fill): Box =
      Box(
        dimensions,
        fill,
        Stroke.None,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

    def apply(dimensions: Rectangle, fill: Fill, stroke: Stroke): Box =
      Box(
        dimensions,
        fill,
        stroke,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

  }

  /** Draws a coloured circle from it's center outwards.
    */
  final case class Circle(
      circle: C,
      fill: Fill,
      stroke: Stroke,
      lighting: LightingModel,
      eventHandlerEnabled: Boolean,
      eventHandler: ((Circle, GlobalEvent)) => Option[GlobalEvent],
      rotation: Radians,
      scale: Vector2,
      ref: Point,
      flip: Flip,
      shaderId: Option[ShaderId]
  ) extends Shape[Circle] {

    lazy val position: Point =
      circle.center - circle.radius - (stroke.width / 2)
    lazy val size: Size =
      Size(circle.radius * 2) + stroke.width

    @deprecated("Use `withFill` instead")
    def withFillColor(newFill: Fill): Circle =
      this.copy(fill = newFill)
    def withFill(newFill: Fill): Circle =
      this.copy(fill = newFill)
    def modifyFill(modifier: Fill => Fill): Circle =
      this.copy(fill = modifier(fill))

    def withStroke(newStroke: Stroke): Circle =
      this.copy(stroke = newStroke)
    def modifyStroke(modifier: Stroke => Stroke): Circle =
      this.copy(stroke = modifier(stroke))

    def withStrokeColor(newStrokeColor: RGBA): Circle =
      this.copy(stroke = stroke.withColor(newStrokeColor))

    def withStrokeWidth(newWidth: Int): Circle =
      this.copy(stroke = stroke.withWidth(newWidth))

    def withRadius(newRadius: Int): Circle =
      this.copy(circle = circle.withRadius(newRadius))
    def resizeTo(newRadius: Int): Circle =
      withRadius(newRadius)
    def resizeBy(amount: Int): Circle =
      withRadius(circle.radius + amount)

    def withLighting(newLighting: LightingModel): Circle =
      this.copy(lighting = newLighting)
    def modifyLighting(modifier: LightingModel => LightingModel): Circle =
      this.copy(lighting = modifier(lighting))

    def moveTo(pt: Point): Circle =
      this.copy(circle = circle.moveTo(pt))
    def moveTo(x: Int, y: Int): Circle =
      moveTo(Point(x, y))
    def withPosition(newPosition: Point): Circle =
      moveTo(newPosition)

    def moveBy(pt: Point): Circle =
      this.copy(circle = circle.moveBy(pt))
    def moveBy(x: Int, y: Int): Circle =
      moveBy(Point(x, y))

    def rotateTo(angle: Radians): Circle =
      this.copy(rotation = angle)
    def rotateBy(angle: Radians): Circle =
      rotateTo(rotation + angle)
    def withRotation(newRotation: Radians): Circle =
      rotateTo(newRotation)

    def scaleBy(amount: Vector2): Circle =
      this.copy(scale = scale * amount)
    def scaleBy(x: Double, y: Double): Circle =
      scaleBy(Vector2(x, y))
    def withScale(newScale: Vector2): Circle =
      this.copy(scale = newScale)

    def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Circle =
      this.copy(circle = circle.moveTo(newPosition), rotation = newRotation, scale = newScale)

    def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Circle =
      transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

    def withRef(newRef: Point): Circle =
      this.copy(ref = newRef)
    def withRef(x: Int, y: Int): Circle =
      withRef(Point(x, y))

    def flipHorizontal(isFlipped: Boolean): Circle =
      this.copy(flip = flip.withHorizontalFlip(isFlipped))
    def flipVertical(isFlipped: Boolean): Circle =
      this.copy(flip = flip.withVerticalFlip(isFlipped))
    def withFlip(newFlip: Flip): Circle =
      this.copy(flip = newFlip)

    def withShaderId(newShaderId: ShaderId): Circle =
      this.copy(shaderId = Option(newShaderId))

    def withEventHandler(f: ((Circle, GlobalEvent)) => Option[GlobalEvent]): Circle =
      this.copy(eventHandler = f, eventHandlerEnabled = true)
    def onEvent(f: PartialFunction[(Circle, GlobalEvent), GlobalEvent]): Circle =
      withEventHandler(f.lift)
    def enableEvents: Circle =
      this.copy(eventHandlerEnabled = true)
    def disableEvents: Circle =
      this.copy(eventHandlerEnabled = false)

  }
  object Circle {

    def apply(center: Point, radius: Int, fill: Fill): Circle =
      Circle(
        C(center, radius),
        fill,
        Stroke.None,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

    def apply(circle: C, fill: Fill): Circle =
      Circle(
        circle,
        fill,
        Stroke.None,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

    def apply(center: Point, radius: Int, fill: Fill, stroke: Stroke): Circle =
      Circle(
        C(center, radius),
        fill,
        stroke,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

    def apply(circle: C, fill: Fill, stroke: Stroke): Circle =
      Circle(
        circle,
        fill,
        stroke,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

  }

  /** Draws a straight line.
    */
  final case class Line(
      start: Point,
      end: Point,
      stroke: Stroke,
      lighting: LightingModel,
      eventHandlerEnabled: Boolean,
      eventHandler: ((Line, GlobalEvent)) => Option[GlobalEvent],
      rotation: Radians,
      scale: Vector2,
      ref: Point,
      flip: Flip,
      shaderId: Option[ShaderId]
  ) extends Shape[Line] {

    lazy val position: Point =
      Point(Math.min(start.x, end.x), Math.min(start.y, end.y)) - (stroke.width / 2)

    lazy val size: Size =
      Rectangle
        .fromPoints(
          position,
          Point(Math.max(start.x, end.x), Math.max(start.y, end.y)) + stroke.width
        )
        .size

    def withStroke(newStroke: Stroke): Line =
      this.copy(stroke = newStroke)
    def modifyStroke(modifier: Stroke => Stroke): Line =
      this.copy(stroke = modifier(stroke))

    def withStrokeColor(newStrokeColor: RGBA): Line =
      this.copy(stroke = stroke.withColor(newStrokeColor))

    def withStrokeWidth(newWidth: Int): Line =
      this.copy(stroke = stroke.withWidth(newWidth))

    def withLighting(newLighting: LightingModel): Line =
      this.copy(lighting = newLighting)
    def modifyLighting(modifier: LightingModel => LightingModel): Line =
      this.copy(lighting = modifier(lighting))

    def moveTo(newPosition: Point): Line =
      this.copy(start = newPosition, end = newPosition + (end - start))
    def moveTo(x: Int, y: Int): Line =
      moveTo(Point(x, y))

    def moveBy(amount: Point): Line =
      moveTo(start + amount)
    def moveBy(x: Int, y: Int): Line =
      moveBy(Point(x, y))

    def moveStartTo(newPosition: Point): Line =
      this.copy(start = newPosition)
    def moveStartTo(x: Int, y: Int): Line =
      moveStartTo(Point(x, y))
    def moveStartBy(amount: Point): Line =
      moveStartTo(start + amount)
    def moveStartBy(x: Int, y: Int): Line =
      moveStartBy(Point(x, y))

    def moveEndTo(newPosition: Point): Line =
      this.copy(end = newPosition)
    def moveEndTo(x: Int, y: Int): Line =
      moveEndTo(Point(x, y))
    def moveEndBy(amount: Point): Line =
      moveEndTo(end + amount)
    def moveEndBy(x: Int, y: Int): Line =
      moveEndBy(Point(x, y))

    def withPosition(newPosition: Point): Line =
      moveTo(newPosition)

    def rotateTo(angle: Radians): Line =
      this.copy(rotation = angle)
    def rotateBy(angle: Radians): Line =
      rotateTo(rotation + angle)
    def withRotation(newRotation: Radians): Line =
      rotateTo(newRotation)

    def scaleBy(amount: Vector2): Line =
      this.copy(scale = scale * amount)
    def scaleBy(x: Double, y: Double): Line =
      scaleBy(Vector2(x, y))
    def withScale(newScale: Vector2): Line =
      this.copy(scale = newScale)

    def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Line =
      this.copy(start = newPosition, rotation = newRotation, scale = newScale)

    def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Line =
      transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

    def withRef(newRef: Point): Line =
      this.copy(ref = newRef)
    def withRef(x: Int, y: Int): Line =
      withRef(Point(x, y))

    def flipHorizontal(isFlipped: Boolean): Line =
      this.copy(flip = flip.withHorizontalFlip(isFlipped))
    def flipVertical(isFlipped: Boolean): Line =
      this.copy(flip = flip.withVerticalFlip(isFlipped))
    def withFlip(newFlip: Flip): Line =
      this.copy(flip = newFlip)

    def withShaderId(newShaderId: ShaderId): Line =
      this.copy(shaderId = Option(newShaderId))

    def withEventHandler(f: ((Line, GlobalEvent)) => Option[GlobalEvent]): Line =
      this.copy(eventHandler = f, eventHandlerEnabled = true)
    def onEvent(f: PartialFunction[(Line, GlobalEvent), GlobalEvent]): Line =
      withEventHandler(f.lift)
    def enableEvents: Line =
      this.copy(eventHandlerEnabled = true)
    def disableEvents: Line =
      this.copy(eventHandlerEnabled = false)
  }
  object Line {

    def apply(start: Point, end: Point, stroke: Stroke): Line =
      Line(
        start,
        end,
        stroke,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

  }

  /** Draws an arbitrary polygon with up to 16 vertices.
    */
  final case class Polygon(
      vertices: Batch[Point],
      fill: Fill,
      stroke: Stroke,
      lighting: LightingModel,
      eventHandlerEnabled: Boolean,
      eventHandler: ((Polygon, GlobalEvent)) => Option[GlobalEvent],
      rotation: Radians,
      scale: Vector2,
      ref: Point,
      flip: Flip,
      shaderId: Option[ShaderId]
  ) extends Shape[Polygon] {

    private lazy val verticesBounds: Rectangle =
      Rectangle.fromPointCloud(vertices).expand(stroke.width / 2)

    lazy val position: Point =
      verticesBounds.position

    lazy val size: Size =
      verticesBounds.size

    @deprecated("Use `withFill` instead")
    def withFillColor(newFill: Fill): Polygon =
      this.copy(fill = newFill)
    def withFill(newFill: Fill): Polygon =
      this.copy(fill = newFill)
    def modifyFill(modifier: Fill => Fill): Polygon =
      this.copy(fill = modifier(fill))

    def withStroke(newStroke: Stroke): Polygon =
      this.copy(stroke = newStroke)
    def modifyStroke(modifier: Stroke => Stroke): Polygon =
      this.copy(stroke = modifier(stroke))

    def withStrokeColor(newStrokeColor: RGBA): Polygon =
      this.copy(stroke = stroke.withColor(newStrokeColor))

    def withStrokeWidth(newWidth: Int): Polygon =
      this.copy(stroke = stroke.withWidth(newWidth))

    def withLighting(newLighting: LightingModel): Polygon =
      this.copy(lighting = newLighting)
    def modifyLighting(modifier: LightingModel => LightingModel): Polygon =
      this.copy(lighting = modifier(lighting))

    private def relativeShift(by: Point): Batch[Point] =
      vertices.map(_.moveBy(by - position))

    def moveTo(pt: Point): Polygon =
      this.copy(vertices = relativeShift(pt))
    def moveTo(x: Int, y: Int): Polygon =
      moveTo(Point(x, y))
    def withPosition(newPosition: Point): Polygon =
      moveTo(newPosition)

    def moveBy(pt: Point): Polygon =
      moveTo(position + pt)
    def moveBy(x: Int, y: Int): Polygon =
      moveBy(Point(x, y))

    def rotateTo(angle: Radians): Polygon =
      this.copy(rotation = angle)
    def rotateBy(angle: Radians): Polygon =
      rotateTo(rotation + angle)
    def withRotation(newRotation: Radians): Polygon =
      rotateTo(newRotation)

    def scaleBy(amount: Vector2): Polygon =
      this.copy(scale = scale * amount)
    def scaleBy(x: Double, y: Double): Polygon =
      scaleBy(Vector2(x, y))
    def withScale(newScale: Vector2): Polygon =
      this.copy(scale = newScale)

    def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Polygon =
      this.copy(vertices = relativeShift(newPosition), rotation = newRotation, scale = newScale)

    def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Polygon =
      transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

    def withRef(newRef: Point): Polygon =
      this.copy(ref = newRef)
    def withRef(x: Int, y: Int): Polygon =
      withRef(Point(x, y))

    def flipHorizontal(isFlipped: Boolean): Polygon =
      this.copy(flip = flip.withHorizontalFlip(isFlipped))
    def flipVertical(isFlipped: Boolean): Polygon =
      this.copy(flip = flip.withVerticalFlip(isFlipped))
    def withFlip(newFlip: Flip): Polygon =
      this.copy(flip = newFlip)

    def withShaderId(newShaderId: ShaderId): Polygon =
      this.copy(shaderId = Option(newShaderId))

    def withEventHandler(f: ((Polygon, GlobalEvent)) => Option[GlobalEvent]): Polygon =
      this.copy(eventHandler = f, eventHandlerEnabled = true)
    def onEvent(f: PartialFunction[(Polygon, GlobalEvent), GlobalEvent]): Polygon =
      withEventHandler(f.lift)
    def enableEvents: Polygon =
      this.copy(eventHandlerEnabled = true)
    def disableEvents: Polygon =
      this.copy(eventHandlerEnabled = false)

  }
  object Polygon {

    def apply(vertices: Batch[Point], fill: Fill): Polygon =
      Polygon(
        vertices,
        fill,
        Stroke.None,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

    def apply(vertices: Batch[Point], fill: Fill, stroke: Stroke): Polygon =
      Polygon(
        vertices,
        fill,
        stroke,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

    def apply(fill: Fill, stroke: Stroke)(vertices: Point*): Polygon =
      Polygon(
        Batch.fromSeq(vertices),
        fill,
        stroke,
        LightingModel.Unlit,
        false,
        Function.const(None),
        Radians.zero,
        Vector2.one,
        Point.zero,
        Flip.default,
        None
      )

  }

  def fillType(fill: Fill): Float =
    fill match {
      case _: Fill.Color          => 0.0f
      case _: Fill.LinearGradient => 1.0f
      case _: Fill.RadialGradient => 2.0f
    }

  def toShaderData(shape: Shape[?], bounds: Rectangle): ShaderData =
    shape match
      case s: Shape.Box =>
        // A terrible fix, but it works. In cases where we have a perfect aspect
        // division, like 1.0 to 0.5, the resulting SDF is a jagged line. So by
        // crudly adding a very small number, we avoid perfect divisions and get
        // nice straight edges...
        val avoidPerfection = 0.00001

        val aspect: Vector2 =
          if (bounds.size.width > bounds.size.height)
            Vector2(1.0, (bounds.size.height.toDouble / bounds.size.width.toDouble) + avoidPerfection)
          else
            Vector2((bounds.size.width.toDouble / bounds.size.height.toDouble) + avoidPerfection, 1.0)

        val shapeUniformBlock =
          UniformBlock(
            UniformBlockName("IndigoShapeData"),
            // ASPECT_RATIO (vec2), STROKE_WIDTH (float), FILL_TYPE (float), STROKE_COLOR (vec4)
            Batch(
              Uniform("Shape_DATA") -> rawJSArray(
                scalajs.js.Array[Float](
                  aspect.x.toFloat,
                  aspect.y.toFloat,
                  s.stroke.width.toFloat,
                  fillType(s.fill),
                  s.stroke.color.r.toFloat,
                  s.stroke.color.g.toFloat,
                  s.stroke.color.b.toFloat,
                  s.stroke.color.a.toFloat
                )
              )
            ) ++ s.fill.toUniformData("SHAPE")
          )

        s.lighting match {
          case Unlit =>
            ShaderData(
              s.shaderId.getOrElse(StandardShaders.ShapeBox.id),
              Batch(shapeUniformBlock)
            )

          case l: Lit =>
            l.toShaderData(s.shaderId.getOrElse(StandardShaders.LitShapeBox.id), None, Batch(shapeUniformBlock))
        }

      case s: Shape.Circle =>
        val shapeUniformBlock =
          UniformBlock(
            UniformBlockName("IndigoShapeData"),
            // STROKE_WIDTH (float), FILL_TYPE (float), STROKE_COLOR (vec4)
            Batch(
              Uniform("Shape_DATA") -> rawJSArray(
                scalajs.js.Array[Float](
                  s.stroke.width.toFloat,
                  fillType(s.fill),
                  0.0f,
                  0.0f,
                  s.stroke.color.r.toFloat,
                  s.stroke.color.g.toFloat,
                  s.stroke.color.b.toFloat,
                  s.stroke.color.a.toFloat
                )
              )
            ) ++ s.fill.toUniformData("SHAPE")
          )

        s.lighting match {
          case Unlit =>
            ShaderData(
              s.shaderId.getOrElse(StandardShaders.ShapeCircle.id),
              Batch(shapeUniformBlock)
            )

          case l: Lit =>
            l.toShaderData(s.shaderId.getOrElse(StandardShaders.LitShapeCircle.id), None, Batch(shapeUniformBlock))
        }

      case s: Shape.Line =>
        // A terrible fix, but it works. In cases where we have a perfect aspect
        // division, like 1.0 to 0.5, the resulting SDF is a jagged line. So by
        // crudly adding a very small number, we avoid perfect divisions and get
        // nice straight edges...
        val avoidPerfection = 0.00001f

        // Relative to bounds
        val ss = s.start - bounds.position + (s.stroke.width / 2)
        val ee = s.end - bounds.position + (s.stroke.width / 2)

        val shapeUniformBlock =
          UniformBlock(
            UniformBlockName("IndigoShapeData"),
            // STROKE_WIDTH (float), STROKE_COLOR (vec4), START (vec2), END (vec2)
            Batch(
              Uniform("Shape_DATA") -> rawJSArray(
                scalajs.js.Array[Float](
                  s.stroke.width.toFloat,
                  0.0f,
                  0.0f,
                  0.0f,
                  s.stroke.color.r.toFloat,
                  s.stroke.color.g.toFloat,
                  s.stroke.color.b.toFloat,
                  s.stroke.color.a.toFloat,
                  ss.x.toFloat + avoidPerfection,
                  ss.y.toFloat + avoidPerfection,
                  ee.x.toFloat + avoidPerfection,
                  ee.y.toFloat + avoidPerfection
                )
              )
            )
          )

        s.lighting match {
          case Unlit =>
            ShaderData(
              s.shaderId.getOrElse(StandardShaders.ShapeLine.id),
              Batch(shapeUniformBlock)
            )

          case l: Lit =>
            l.toShaderData(s.shaderId.getOrElse(StandardShaders.LitShapeLine.id), None, Batch(shapeUniformBlock))
        }

      case s: Shape.Polygon =>
        // A terrible fix, but it works. In cases where we have a perfect aspect
        // division, like 1.0 to 0.5, the resulting SDF is a jagged line. So by
        // crudly adding a very small number, we avoid perfect divisions and get
        // nice straight edges...
        val avoidPerfection = 0.00001

        val verts: Batch[vec2] =
          s.vertices.map { v =>
            vec2(
              (v.x - bounds.x).toFloat + avoidPerfection,
              (v.y - bounds.y).toFloat + avoidPerfection
            )
          }

        val shapeUniformBlock =
          UniformBlock(
            UniformBlockName("IndigoShapeData"),
            // STROKE_WIDTH (float), FILL_TYPE (float), COUNT (float), STROKE_COLOR (vec4)
            Batch(
              Uniform("Shape_DATA") -> rawJSArray(
                scalajs.js.Array[Float](
                  s.stroke.width.toFloat,
                  fillType(s.fill),
                  verts.length.toFloat,
                  0.0f,
                  s.stroke.color.r.toFloat,
                  s.stroke.color.g.toFloat,
                  s.stroke.color.b.toFloat,
                  s.stroke.color.a.toFloat
                )
              )
            ) ++ s.fill.toUniformData("SHAPE") ++ (Batch(Uniform("VERTICES") -> array[vec2](16, verts.toArray)))
          )

        s.lighting match {
          case Unlit =>
            ShaderData(
              s.shaderId.getOrElse(StandardShaders.ShapePolygon.id),
              Batch(shapeUniformBlock)
            )

          case l: Lit =>
            l.toShaderData(s.shaderId.getOrElse(StandardShaders.LitShapePolygon.id), None, Batch(shapeUniformBlock))
        }

end Shape
