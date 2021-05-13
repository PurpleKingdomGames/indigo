package indigo.shared.scenegraph

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Depth
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Flip
import indigo.shared.datatypes.Rectangle
import indigo.shared.materials.ShaderData
import indigo.shared.materials.LightingModel
import indigo.shared.shader.Uniform
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.ShaderPrimitive._
import indigo.shared.datatypes.RGBA
import indigo.shared.shader.StandardShaders
import indigo.shared.datatypes.Fill
import indigo.shared.datatypes.Stroke
import indigo.shared.materials.LightingModel.Unlit
import indigo.shared.materials.LightingModel.Lit
import indigo.shared.shader.ShaderId
import indigo.shared.BoundaryLocator

sealed trait Shape extends CompositeNode with Cloneable with SpatialModifiers[Shape] derives CanEqual {
  def moveTo(pt: Point): Shape
  def moveTo(x: Int, y: Int): Shape
  def withPosition(newPosition: Point): Shape

  def moveBy(pt: Point): Shape
  def moveBy(x: Int, y: Int): Shape

  def rotateTo(angle: Radians): Shape
  def rotateBy(angle: Radians): Shape
  def withRotation(newRotation: Radians): Shape

  def scaleBy(amount: Vector2): Shape
  def scaleBy(x: Double, y: Double): Shape
  def withScale(newScale: Vector2): Shape

  def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Shape
  def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Shape

  def withDepth(newDepth: Depth): Shape

  def flipHorizontal(isFlipped: Boolean): Shape
  def flipVertical(isFlipped: Boolean): Shape
  def withFlip(newFlip: Flip): Shape
}

object Shape {

  final case class Box(
      dimensions: Rectangle,
      fill: Fill,
      stroke: Stroke,
      lighting: LightingModel,
      rotation: Radians,
      scale: Vector2,
      depth: Depth,
      ref: Point,
      flip: Flip,
      shaderId: Option[ShaderId]
  ) extends Shape {

    lazy val position: Point =
      dimensions.position - (stroke.width / 2)

    def calculatedBounds(locator: BoundaryLocator): Option[Rectangle] =
      locator.findBounds(this)

    def withDimensions(newDimensions: Rectangle): Box =
      this.copy(dimensions = newDimensions)

    def resize(size: Point): Box =
      this.copy(dimensions = dimensions.resize(size))

    def withFill(newFill: Fill): Box =
      this.copy(fill = newFill)

    def withStroke(newStroke: Stroke): Box =
      this.copy(stroke = newStroke)

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

    def withDepth(newDepth: Depth): Box =
      this.copy(depth = newDepth)

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
  }
  object Box {

    def apply(dimensions: Rectangle, fill: Fill): Box =
      Box(
        dimensions,
        fill,
        Stroke.None,
        LightingModel.Unlit,
        Radians.zero,
        Vector2.one,
        Depth(1),
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
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default,
        None
      )

  }

  final case class Circle(
      center: Point,
      radius: Int,
      fill: Fill,
      stroke: Stroke,
      lighting: LightingModel,
      rotation: Radians,
      scale: Vector2,
      depth: Depth,
      ref: Point,
      flip: Flip,
      shaderId: Option[ShaderId]
  ) extends Shape {

    lazy val position: Point =
      center - radius - (stroke.width / 2)

    def calculatedBounds(locator: BoundaryLocator): Option[Rectangle] =
      locator.findBounds(this)

    def withFillColor(newFill: Fill): Circle =
      this.copy(fill = newFill)

    def withStroke(newStroke: Stroke): Circle =
      this.copy(stroke = newStroke)

    def withStrokeColor(newStrokeColor: RGBA): Circle =
      this.copy(stroke = stroke.withColor(newStrokeColor))

    def withStrokeWidth(newWidth: Int): Circle =
      this.copy(stroke = stroke.withWidth(newWidth))

    def withRadius(newRadius: Int): Circle =
      this.copy(radius = newRadius)
    def resizeTo(newRadius: Int): Circle =
      withRadius(newRadius)
    def resizeBy(amount: Int): Circle =
      withRadius(radius + amount)

    def withLighting(newLighting: LightingModel): Circle =
      this.copy(lighting = newLighting)
    def modifyLighting(modifier: LightingModel => LightingModel): Circle =
      this.copy(lighting = modifier(lighting))

    def moveTo(pt: Point): Circle =
      this.copy(center = pt)
    def moveTo(x: Int, y: Int): Circle =
      moveTo(Point(x, y))
    def withPosition(newPosition: Point): Circle =
      moveTo(newPosition)

    def moveBy(pt: Point): Circle =
      this.copy(center = center + pt)
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
      this.copy(center = newPosition, rotation = newRotation, scale = newScale)

    def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Circle =
      transformTo(position + positionDiff, rotation + rotationDiff, scale * scaleDiff)

    def withDepth(newDepth: Depth): Circle =
      this.copy(depth = newDepth)

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

  }
  object Circle {

    def apply(center: Point, radius: Int, fill: Fill): Circle =
      Circle(
        center,
        radius,
        fill,
        Stroke.None,
        LightingModel.Unlit,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default,
        None
      )

    def apply(center: Point, radius: Int, fill: Fill, stroke: Stroke): Circle =
      Circle(
        center,
        radius,
        fill,
        stroke,
        LightingModel.Unlit,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default,
        None
      )

  }

  final case class Line(
      start: Point,
      end: Point,
      stroke: Stroke,
      lighting: LightingModel,
      rotation: Radians,
      scale: Vector2,
      depth: Depth,
      ref: Point,
      flip: Flip,
      shaderId: Option[ShaderId]
  ) extends Shape {

    lazy val position: Point =
      val x = Math.min(start.x, end.x)
      val y = Math.min(start.y, end.y)
      Point(x, y) - (stroke.width / 2)

    def calculatedBounds(locator: BoundaryLocator): Option[Rectangle] =
      locator.findBounds(this)

    def withStroke(newStroke: Stroke): Line =
      this.copy(stroke = newStroke)

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

    def withDepth(newDepth: Depth): Line =
      this.copy(depth = newDepth)

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
  }
  object Line {

    def apply(start: Point, end: Point, stroke: Stroke): Line =
      Line(
        start,
        end,
        stroke,
        LightingModel.Unlit,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default,
        None
      )

  }

  final case class Polygon(
      vertices: List[Point],
      fill: Fill,
      stroke: Stroke,
      lighting: LightingModel,
      rotation: Radians,
      scale: Vector2,
      depth: Depth,
      ref: Point,
      flip: Flip,
      shaderId: Option[ShaderId]
  ) extends Shape {

    lazy val position: Point =
      Rectangle.fromPointCloud(vertices).expand(stroke.width / 2).position

    def calculatedBounds(locator: BoundaryLocator): Option[Rectangle] =
      locator.findBounds(this)

    def withFillColor(newFill: Fill): Polygon =
      this.copy(fill = newFill)

    def withStroke(newStroke: Stroke): Polygon =
      this.copy(stroke = newStroke)

    def withStrokeColor(newStrokeColor: RGBA): Polygon =
      this.copy(stroke = stroke.withColor(newStrokeColor))

    def withStrokeWidth(newWidth: Int): Polygon =
      this.copy(stroke = stroke.withWidth(newWidth))

    def withLighting(newLighting: LightingModel): Polygon =
      this.copy(lighting = newLighting)
    def modifyLighting(modifier: LightingModel => LightingModel): Polygon =
      this.copy(lighting = modifier(lighting))

    private def relativeShift(by: Point): List[Point] =
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

    def withDepth(newDepth: Depth): Polygon =
      this.copy(depth = newDepth)

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

  }
  object Polygon {

    def apply(vertices: List[Point], fill: Fill): Polygon =
      Polygon(
        vertices,
        fill,
        Stroke.None,
        LightingModel.Unlit,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default,
        None
      )

    def apply(vertices: List[Point], fill: Fill, stroke: Stroke): Polygon =
      Polygon(
        vertices,
        fill,
        stroke,
        LightingModel.Unlit,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default,
        None
      )

    def apply(fill: Fill, stroke: Stroke)(vertices: Point*): Polygon =
      Polygon(
        vertices.toList,
        fill,
        stroke,
        LightingModel.Unlit,
        Radians.zero,
        Vector2.one,
        Depth(1),
        Point.zero,
        Flip.default,
        None
      )

  }

  def gradientUniforms(fill: Fill): List[(Uniform, vec4)] =
    fill match {
      case Fill.Color(color) =>
        val c = vec4(color.r, color.g, color.b, color.a)
        List(
          Uniform("GRADIENT_FROM_TO")    -> vec4(0.0d),
          Uniform("GRADIENT_FROM_COLOR") -> c,
          Uniform("GRADIENT_TO_COLOR")   -> c
        )

      case Fill.LinearGradient(fromPoint, fromColor, toPoint, toColor) =>
        List(
          Uniform("GRADIENT_FROM_TO") -> vec4(
            fromPoint.x.toDouble,
            fromPoint.y.toDouble,
            toPoint.x.toDouble,
            toPoint.y.toDouble
          ),
          Uniform("GRADIENT_FROM_COLOR") -> vec4(fromColor.r, fromColor.g, fromColor.b, fromColor.a),
          Uniform("GRADIENT_TO_COLOR")   -> vec4(toColor.r, toColor.g, toColor.b, toColor.a)
        )

      case Fill.RadialGradient(fromPoint, fromColor, toPoint, toColor) =>
        List(
          Uniform("GRADIENT_FROM_TO") -> vec4(
            fromPoint.x.toDouble,
            fromPoint.y.toDouble,
            toPoint.x.toDouble,
            toPoint.y.toDouble
          ),
          Uniform("GRADIENT_FROM_COLOR") -> vec4(fromColor.r, fromColor.g, fromColor.b, fromColor.a),
          Uniform("GRADIENT_TO_COLOR")   -> vec4(toColor.r, toColor.g, toColor.b, toColor.a)
        )
    }

  def fillType(fill: Fill): float =
    fill match {
      case _: Fill.Color          => float(0.0)
      case _: Fill.LinearGradient => float(1.0)
      case _: Fill.RadialGradient => float(2.0)
    }

  def toShaderData(shape: Shape, bounds: Rectangle): ShaderData =
    shape match
      case s: Shape.Box =>
        val aspect: Vector2 =
          if (bounds.size.x > bounds.size.y)
            Vector2(1.0, bounds.size.y.toDouble / bounds.size.x.toDouble)
          else
            Vector2(bounds.size.x.toDouble / bounds.size.y.toDouble, 1.0)

        val shapeUniformBlock =
          UniformBlock(
            "IndigoShapeData",
            List(
              Uniform("ASPECT_RATIO") -> vec2(aspect.x, aspect.y),
              Uniform("STROKE_WIDTH") -> float(s.stroke.width.toFloat),
              Uniform("FILL_TYPE")    -> fillType(s.fill),
              Uniform("STROKE_COLOR") -> vec4(s.stroke.color.r, s.stroke.color.g, s.stroke.color.b, s.stroke.color.a)
            ) ++ gradientUniforms(s.fill)
          )

        s.lighting match {
          case Unlit =>
            ShaderData(
              s.shaderId.getOrElse(StandardShaders.ShapeBox.id),
              shapeUniformBlock
            )

          case l: Lit =>
            l.toShaderData(s.shaderId.getOrElse(StandardShaders.LitShapeBox.id))
              .addUniformBlock(shapeUniformBlock)
        }

      case s: Shape.Circle =>
        val shapeUniformBlock =
          UniformBlock(
            "IndigoShapeData",
            List(
              Uniform("STROKE_WIDTH") -> float(s.stroke.width.toFloat),
              Uniform("FILL_TYPE")    -> fillType(s.fill),
              Uniform("STROKE_COLOR") -> vec4(s.stroke.color.r, s.stroke.color.g, s.stroke.color.b, s.stroke.color.a)
            ) ++ gradientUniforms(s.fill)
          )

        s.lighting match {
          case Unlit =>
            ShaderData(
              s.shaderId.getOrElse(StandardShaders.ShapeCircle.id),
              shapeUniformBlock
            )

          case l: Lit =>
            l.toShaderData(s.shaderId.getOrElse(StandardShaders.LitShapeCircle.id))
              .addUniformBlock(shapeUniformBlock)
        }

      case s: Shape.Line =>
        // val bounds: Rectangle =
        //   Rectangle.fromTwoPoints(s.start, s.end)

        // Relative to bounds
        val ss = s.start - bounds.position + (s.stroke.width / 2)
        val ee = s.end - bounds.position + (s.stroke.width / 2)

        val shapeUniformBlock =
          UniformBlock(
            "IndigoShapeData",
            List(
              Uniform("STROKE_WIDTH") -> float(s.stroke.width.toFloat),
              Uniform("STROKE_COLOR") -> vec4(s.stroke.color.r, s.stroke.color.g, s.stroke.color.b, s.stroke.color.a),
              Uniform("START")        -> vec2(ss.x.toFloat, ss.y.toFloat),
              Uniform("END")          -> vec2(ee.x.toFloat, ee.y.toFloat)
            )
          )

        s.lighting match {
          case Unlit =>
            ShaderData(
              s.shaderId.getOrElse(StandardShaders.ShapeLine.id),
              shapeUniformBlock
            )

          case l: Lit =>
            l.toShaderData(s.shaderId.getOrElse(StandardShaders.LitShapeLine.id))
              .addUniformBlock(shapeUniformBlock)
        }

      case s: Shape.Polygon =>
        val verts: Array[vec2] =
          s.vertices.map { v =>
            vec2(
              (v.x - bounds.x).toFloat,
              (v.y - bounds.y).toFloat
            )
          }.toArray

        val shapeUniformBlock =
          UniformBlock(
            "IndigoShapeData",
            List(
              Uniform("STROKE_WIDTH") -> float(s.stroke.width.toFloat),
              Uniform("FILL_TYPE")    -> fillType(s.fill),
              Uniform("COUNT")        -> float(verts.length.toFloat),
              Uniform("STROKE_COLOR") -> vec4(s.stroke.color.r, s.stroke.color.g, s.stroke.color.b, s.stroke.color.a)
            ) ++ gradientUniforms(s.fill) ++ List(Uniform("VERTICES") -> array(16, verts))
          )

        s.lighting match {
          case Unlit =>
            ShaderData(
              s.shaderId.getOrElse(StandardShaders.ShapePolygon.id),
              shapeUniformBlock
            )

          case l: Lit =>
            l.toShaderData(s.shaderId.getOrElse(StandardShaders.LitShapePolygon.id))
              .addUniformBlock(shapeUniformBlock)
        }
}
