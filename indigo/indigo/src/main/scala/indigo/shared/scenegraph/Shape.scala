package indigo.shared.scenegraph

import indigo.shared.scenegraph.syntax.BasicSpatial
import indigo.shared.scenegraph.syntax.Spatial

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Size
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

sealed trait Shape extends RenderNode with Cloneable derives CanEqual, BasicSpatial, Spatial {

  def calculatedBounds(locator: BoundaryLocator): Rectangle =
    val rect = locator.shapeBounds(this)
    BoundaryLocator.findBounds(this, rect.position, rect.size, ref)
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
    lazy val size: Size =
      dimensions.size + stroke.width

    def withDimensions(newDimensions: Rectangle): Box =
      this.copy(dimensions = newDimensions)

    def resize(size: Size): Box =
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

    def withDepth(newDepth: Depth): Box =
      this.copy(depth = newDepth)

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

    given BasicSpatial[Box] with
      extension (box: Box)
        def withPosition(newPosition: Point): Box =
          box.copy(dimensions = box.dimensions.moveTo(newPosition))
        def withRotation(newRotation: Radians): Box =
          box.copy(rotation = newRotation)
        def withScale(newScale: Vector2): Box =
          box.copy(scale = newScale)
        def withDepth(newDepth: Depth): Box =
          box.copy(depth = newDepth)
        def withFlip(newFlip: Flip): Box =
          box.copy(flip = newFlip)

    given spatialBox(using bs: BasicSpatial[Box]): Spatial[Box] with
      extension (box: Box)
        def moveBy(pt: Point): Box =
          box.moveTo(box.position + pt)
        def moveBy(x: Int, y: Int): Box =
          moveBy(Point(x, y))

        def rotateTo(angle: Radians): Box =
          box.withRotation(angle)
        def rotateBy(angle: Radians): Box =
          rotateTo(box.rotation + angle)

        def scaleBy(amount: Vector2): Box =
          box.withScale(box.scale * amount)
        def scaleBy(x: Double, y: Double): Box =
          scaleBy(Vector2(x, y))

        def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Box =
          box.copy(dimensions = box.dimensions.moveTo(newPosition), rotation = newRotation, scale = newScale)
        def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Box =
          transformTo(box.position + positionDiff, box.rotation + rotationDiff, box.scale * scaleDiff)

        def withRef(newRef: Point): Box =
          box.copy(ref = newRef)
        def withRef(x: Int, y: Int): Box =
          box.copy(ref = Point(x, y))

        def flipHorizontal(isFlipped: Boolean): Box =
          box.withFlip(box.flip.withHorizontalFlip(isFlipped))
        def flipVertical(isFlipped: Boolean): Box =
          box.withFlip(box.flip.withVerticalFlip(isFlipped))
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
    lazy val size: Size =
      Size(radius * 2) + stroke.width

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

    def withDepth(newDepth: Depth): Circle =
      this.copy(depth = newDepth)

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

    given BasicSpatial[Circle] with
      extension (circle: Circle)
        def withPosition(newPosition: Point): Circle =
          circle.copy(center = newPosition)
        def withRotation(newRotation: Radians): Circle =
          circle.copy(rotation = newRotation)
        def withScale(newScale: Vector2): Circle =
          circle.copy(scale = newScale)
        def withDepth(newDepth: Depth): Circle =
          circle.copy(depth = newDepth)
        def withFlip(newFlip: Flip): Circle =
          circle.copy(flip = newFlip)

    given spatialCircle(using bs: BasicSpatial[Circle]): Spatial[Circle] with
      extension (circle: Circle)
        def moveBy(pt: Point): Circle =
          circle.moveTo(circle.center + pt)
        def moveBy(x: Int, y: Int): Circle =
          moveBy(Point(x, y))

        def rotateTo(angle: Radians): Circle =
          circle.withRotation(angle)
        def rotateBy(angle: Radians): Circle =
          rotateTo(circle.rotation + angle)

        def scaleBy(amount: Vector2): Circle =
          circle.withScale(circle.scale * amount)
        def scaleBy(x: Double, y: Double): Circle =
          scaleBy(Vector2(x, y))

        def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Circle =
          circle.copy(center = newPosition, rotation = newRotation, scale = newScale)
        def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Circle =
          transformTo(circle.center + positionDiff, circle.rotation + rotationDiff, circle.scale * scaleDiff)

        def withRef(newRef: Point): Circle =
          circle.copy(ref = newRef)
        def withRef(x: Int, y: Int): Circle =
          circle.copy(ref = Point(x, y))

        def flipHorizontal(isFlipped: Boolean): Circle =
          circle.withFlip(circle.flip.withHorizontalFlip(isFlipped))
        def flipVertical(isFlipped: Boolean): Circle =
          circle.withFlip(circle.flip.withVerticalFlip(isFlipped))

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
      Point(Math.min(start.x, end.x), Math.min(start.y, end.y)) - (stroke.width / 2)

    lazy val size: Size =
      Rectangle
        .fromTwoPoints(
          position,
          Point(Math.max(start.x, end.x), Math.max(start.y, end.y)) + stroke.width
        )
        .size

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

    def withDepth(newDepth: Depth): Line =
      this.copy(depth = newDepth)

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

    given BasicSpatial[Line] with
      extension (line: Line)
        def withPosition(newPosition: Point): Line =
          line.copy(start = newPosition, end = newPosition + (line.end - line.start))
        def withRotation(newRotation: Radians): Line =
          line.copy(rotation = newRotation)
        def withScale(newScale: Vector2): Line =
          line.copy(scale = newScale)
        def withDepth(newDepth: Depth): Line =
          line.copy(depth = newDepth)
        def withFlip(newFlip: Flip): Line =
          line.copy(flip = newFlip)

    given spatialLine(using bs: BasicSpatial[Line]): Spatial[Line] with
      extension (line: Line)
        def moveBy(pt: Point): Line =
          line.moveTo(line.start + pt)
        def moveBy(x: Int, y: Int): Line =
          moveBy(Point(x, y))

        // Extra operations to move line start and end vertices
        def moveStartTo(newPosition: Point): Line =
          line.copy(start = newPosition)
        def moveStartTo(x: Int, y: Int): Line =
          moveStartTo(Point(x, y))
        def moveStartBy(amount: Point): Line =
          moveStartTo(line.start + amount)
        def moveStartBy(x: Int, y: Int): Line =
          moveStartBy(Point(x, y))

        def moveEndTo(newPosition: Point): Line =
          line.copy(end = newPosition)
        def moveEndTo(x: Int, y: Int): Line =
          moveEndTo(Point(x, y))
        def moveEndBy(amount: Point): Line =
          moveEndTo(line.end + amount)
        def moveEndBy(x: Int, y: Int): Line =
          moveEndBy(Point(x, y))
        // End of line extra operations

        def rotateTo(angle: Radians): Line =
          line.withRotation(angle)
        def rotateBy(angle: Radians): Line =
          rotateTo(line.rotation + angle)

        def scaleBy(amount: Vector2): Line =
          line.withScale(line.scale * amount)
        def scaleBy(x: Double, y: Double): Line =
          scaleBy(Vector2(x, y))

        def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Line =
          line.copy(rotation = newRotation, scale = newScale).moveTo(newPosition)
        def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Line =
          transformTo(line.position + positionDiff, line.rotation + rotationDiff, line.scale * scaleDiff)

        def withRef(newRef: Point): Line =
          line.copy(ref = newRef)
        def withRef(x: Int, y: Int): Line =
          line.copy(ref = Point(x, y))

        def flipHorizontal(isFlipped: Boolean): Line =
          line.withFlip(line.flip.withHorizontalFlip(isFlipped))
        def flipVertical(isFlipped: Boolean): Line =
          line.withFlip(line.flip.withVerticalFlip(isFlipped))

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

    private lazy val verticesBounds: Rectangle =
      Rectangle.fromPointCloud(vertices).expand(stroke.width / 2)

    lazy val position: Point =
      verticesBounds.position

    lazy val size: Size =
      verticesBounds.size

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

    def withDepth(newDepth: Depth): Polygon =
      this.copy(depth = newDepth)

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

    given BasicSpatial[Polygon] with
      extension (polygon: Polygon)
        private def relativeShift(by: Point): List[Point] =
          polygon.vertices.map(_.moveBy(by - polygon.position))

        def withPosition(newPosition: Point): Polygon =
          polygon.copy(vertices = relativeShift(newPosition))
        def withRotation(newRotation: Radians): Polygon =
          polygon.copy(rotation = newRotation)
        def withScale(newScale: Vector2): Polygon =
          polygon.copy(scale = newScale)
        def withDepth(newDepth: Depth): Polygon =
          polygon.copy(depth = newDepth)
        def withFlip(newFlip: Flip): Polygon =
          polygon.copy(flip = newFlip)

    given spatialPolygon(using bs: BasicSpatial[Polygon]): Spatial[Polygon] with
      extension (polygon: Polygon)
        def moveBy(pt: Point): Polygon =
          polygon.moveTo(polygon.position + pt)
        def moveBy(x: Int, y: Int): Polygon =
          moveBy(Point(x, y))

        def rotateTo(angle: Radians): Polygon =
          polygon.withRotation(angle)
        def rotateBy(angle: Radians): Polygon =
          rotateTo(polygon.rotation + angle)

        def scaleBy(amount: Vector2): Polygon =
          polygon.withScale(polygon.scale * amount)
        def scaleBy(x: Double, y: Double): Polygon =
          scaleBy(Vector2(x, y))

        def transformTo(newPosition: Point, newRotation: Radians, newScale: Vector2): Polygon =
          polygon.copy(rotation = newRotation, scale = newScale).withPosition(newPosition)
        def transformBy(positionDiff: Point, rotationDiff: Radians, scaleDiff: Vector2): Polygon =
          transformTo(polygon.position + positionDiff, polygon.rotation + rotationDiff, polygon.scale * scaleDiff)

        def withRef(newRef: Point): Polygon =
          polygon.copy(ref = newRef)
        def withRef(x: Int, y: Int): Polygon =
          polygon.copy(ref = Point(x, y))

        def flipHorizontal(isFlipped: Boolean): Polygon =
          polygon.withFlip(polygon.flip.withHorizontalFlip(isFlipped))
        def flipVertical(isFlipped: Boolean): Polygon =
          polygon.withFlip(polygon.flip.withVerticalFlip(isFlipped))
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
          if (bounds.size.width > bounds.size.height)
            Vector2(1.0, bounds.size.height.toDouble / bounds.size.width.toDouble)
          else
            Vector2(bounds.size.width.toDouble / bounds.size.height.toDouble, 1.0)

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
